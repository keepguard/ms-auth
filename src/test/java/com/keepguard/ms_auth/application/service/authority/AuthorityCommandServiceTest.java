package com.keepguard.ms_auth.application.service.authority;

import com.keepguard.ms_auth.application.mapper.AuthorityApplicationMapper;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.dto.authority.*;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.application.dto.authority.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthorityCommandService Tests")
class AuthorityCommandServiceTest {

    private AuthorityCommandService commandService;
    private AuthorityRepositoryPort repository;
    private MetricsPort metricsPort;
    private AuthorityApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        repository = mock(AuthorityRepositoryPort.class);
        metricsPort = mock(MetricsPort.class);
        mapper = mock(AuthorityApplicationMapper.class);
        commandService = new AuthorityCommandService(repository, metricsPort, mapper);
    }

    @Test
    @DisplayName("create salva authority com sucesso")
    void create_shouldSaveAuthority() {
        var cmd = AuthorityCreateCommandDTO.builder()
                .name("READ_USERS")
                .description("desc")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        when(repository.findByName("READ_USERS")).thenReturn(Optional.empty());
        var savedAuthority = Authority.builder()
                .id(UUID.randomUUID())
                .name("READ_USERS")
                .description("desc")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(repository.save(any(Authority.class))).thenReturn(savedAuthority);
        var view = new AuthorityCreateView(savedAuthority.getId(), "READ_USERS", "desc", savedAuthority.getCreatedAt(), savedAuthority.getUpdatedAt());
        when(mapper.toCreateView(savedAuthority)).thenReturn(view);

        var result = commandService.create(cmd);

        assertNotNull(result);
        assertEquals("READ_USERS", result.name());
        verify(repository).findByName("READ_USERS");
        verify(repository).save(any(Authority.class));
        verify(metricsPort).incrementCounter(eq("authority_created_total"), anyMap());
    }

    @Test
    @DisplayName("create lança AlreadyExistsException quando nome já existe")
    void create_shouldThrowAlreadyExistsException() {
        var cmd = AuthorityCreateCommandDTO.builder()
                .name("READ_USERS")
                .description("desc")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var existing = Authority.builder().id(UUID.randomUUID()).name("READ_USERS").build();
        when(repository.findByName("READ_USERS")).thenReturn(Optional.of(existing));

        assertThrows(AlreadyExistsException.class, () -> commandService.create(cmd));

        verify(repository).findByName("READ_USERS");
        verify(repository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("authority_business_errors_total"), anyMap());
    }

    @Test
    @DisplayName("update atualiza authority com sucesso")
    void update_shouldUpdateAuthority() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityUpdateCommandDTO.builder()
                .id(id)
                .name("WRITE_USERS")
                .description("updated desc")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var existing = Authority.builder()
                .id(id)
                .name("READ_USERS")
                .description("old desc")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.findByName("WRITE_USERS")).thenReturn(Optional.empty());
        when(repository.save(any(Authority.class))).thenAnswer(inv -> inv.getArgument(0));
        var view = new AuthorityUpdateView(id, "WRITE_USERS", "updated desc", existing.getCreatedAt(), LocalDateTime.now());
        when(mapper.toUpdateView(any())).thenReturn(view);

        var result = commandService.update(cmd);

        assertNotNull(result);
        assertEquals("WRITE_USERS", result.name());
        verify(repository).findById(id);
        verify(repository).save(any(Authority.class));
        verify(metricsPort).incrementCounter(eq("authority_updated_total"), anyMap());
    }

    @Test
    @DisplayName("update lança NotFoundException quando authority não existe")
    void update_shouldThrowNotFoundExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityUpdateCommandDTO.builder()
                .id(id)
                .name("WRITE_USERS")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commandService.update(cmd));

        verify(repository).findById(id);
        verify(repository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("authority_business_errors_total"), anyMap());
    }

    @Test
    @DisplayName("update lança AlreadyExistsException quando novo nome já existe")
    void update_shouldThrowAlreadyExistsExceptionWhenNameExists() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityUpdateCommandDTO.builder()
                .id(id)
                .name("WRITE_USERS")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var existing = Authority.builder()
                .id(id)
                .name("READ_USERS")
                .build();
        var conflicting = Authority.builder()
                .id(UUID.randomUUID())
                .name("WRITE_USERS")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.findByName("WRITE_USERS")).thenReturn(Optional.of(conflicting));

        assertThrows(AlreadyExistsException.class, () -> commandService.update(cmd));

        verify(repository).findById(id);
        verify(repository).findByName("WRITE_USERS");
        verify(repository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("authority_business_errors_total"), anyMap());
    }

    @Test
    @DisplayName("update permite manter o mesmo nome")
    void update_shouldAllowSameName() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityUpdateCommandDTO.builder()
                .id(id)
                .name("READ_USERS")
                .description("updated desc")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var existing = Authority.builder()
                .id(id)
                .name("READ_USERS")
                .description("old desc")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Authority.class))).thenAnswer(inv -> inv.getArgument(0));
        var view = new AuthorityUpdateView(id, "READ_USERS", "updated desc", existing.getCreatedAt(), LocalDateTime.now());
        when(mapper.toUpdateView(any())).thenReturn(view);

        var result = commandService.update(cmd);

        assertNotNull(result);
        verify(repository).findById(id);
        verify(repository, never()).findByName(anyString());
        verify(repository).save(any(Authority.class));
    }

    @Test
    @DisplayName("delete remove authority com sucesso")
    void delete_shouldDeleteAuthority() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityDeleteCommandDTO.builder()
                .id(id)
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var existing = Authority.builder()
                .id(id)
                .name("READ_USERS")
                .build();
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        commandService.delete(cmd);

        verify(repository).findById(id);
        verify(repository).delete(existing);
        verify(metricsPort).incrementCounter(eq("authority_deleted_total"), anyMap());
    }

    @Test
    @DisplayName("delete lança NotFoundException quando authority não existe")
    void delete_shouldThrowNotFoundExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityDeleteCommandDTO.builder()
                .id(id)
                .xApplicationUuid(UUID.randomUUID())
                .build();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commandService.delete(cmd));

        verify(repository).findById(id);
        verify(repository, never()).delete(any());
        verify(metricsPort).incrementCounter(eq("authority_business_errors_total"), anyMap());
    }
}

