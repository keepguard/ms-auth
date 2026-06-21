package com.keepguard.ms_auth.application.service.authority;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.dto.authority.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthorityUseCaseService Tests")
class AuthorityUseCaseServiceTest {

    private AuthorityUseCaseService useCaseService;
    private AuthorityCommandService commandService;
    private AuthorityQueryService queryService;

    @BeforeEach
    void setUp() {
        commandService = mock(AuthorityCommandService.class);
        queryService = mock(AuthorityQueryService.class);
        useCaseService = new AuthorityUseCaseService(commandService, queryService);
    }

    @Test
    @DisplayName("create delega para commandService")
    void create_shouldDelegateToCommandService() {
        var cmd = AuthorityCreateCommandDTO.builder()
                .name("READ_USERS")
                .description("desc")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var view = new AuthorityCreateView(UUID.randomUUID(), "READ_USERS", "desc", LocalDateTime.now(), LocalDateTime.now());
        when(commandService.create(cmd)).thenReturn(view);

        var result = useCaseService.create(cmd);

        assertNotNull(result);
        assertEquals("READ_USERS", result.name());
        verify(commandService).create(cmd);
    }

    @Test
    @DisplayName("update delega para commandService")
    void update_shouldDelegateToCommandService() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityUpdateCommandDTO.builder()
                .id(id)
                .name("WRITE_USERS")
                .description("desc")
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var view = new AuthorityUpdateView(id, "WRITE_USERS", "desc", LocalDateTime.now(), LocalDateTime.now());
        when(commandService.update(cmd)).thenReturn(view);

        var result = useCaseService.update(cmd);

        assertNotNull(result);
        assertEquals("WRITE_USERS", result.name());
        verify(commandService).update(cmd);
    }

    @Test
    @DisplayName("delete delega para commandService")
    void delete_shouldDelegateToCommandService() {
        UUID id = UUID.randomUUID();
        var cmd = AuthorityDeleteCommandDTO.builder()
                .id(id)
                .xApplicationUuid(UUID.randomUUID())
                .build();

        useCaseService.delete(cmd);

        verify(commandService).delete(cmd);
    }

    @Test
    @DisplayName("findById delega para queryService")
    void findById_shouldDelegateToQueryService() {
        UUID id = UUID.randomUUID();
        var query = AuthorityGetByIdQueryDTO.builder()
                .id(id)
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var view = new AuthorityGetByIdView(id, "READ_USERS", "desc", LocalDateTime.now(), LocalDateTime.now());
        when(queryService.findById(id)).thenReturn(Optional.of(view));

        var result = useCaseService.findById(query);

        assertTrue(result.isPresent());
        assertEquals("READ_USERS", result.get().name());
        verify(queryService).findById(id);
    }

    @Test
    @DisplayName("findByName delega para queryService")
    void findByName_shouldDelegateToQueryService() {
        String name = "READ_USERS";
        var query = AuthorityGetByNameQueryDTO.builder()
                .name(name)
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var view = new AuthorityGetByNameView(UUID.randomUUID(), name, "desc", LocalDateTime.now(), LocalDateTime.now());
        when(queryService.findByName(name)).thenReturn(Optional.of(view));

        var result = useCaseService.findByName(query);

        assertTrue(result.isPresent());
        assertEquals(name, result.get().name());
        verify(queryService).findByName(name);
    }

    @Test
    @DisplayName("findAll (sem paginação) delega para queryService")
    void findAll_shouldDelegateToQueryService() {
        var query = AuthorityGetAllQueryDTO.builder()
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var view = new AuthorityListView(UUID.randomUUID(), "READ_USERS", "desc", LocalDateTime.now(), LocalDateTime.now());
        when(queryService.findAll()).thenReturn(List.of(view));

        var result = useCaseService.findAll(query);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(queryService).findAll();
    }

    @Test
    @DisplayName("findAll (com paginação) delega para queryService")
    void findAllPaginated_shouldDelegateToQueryService() {
        var pageable = PageRequest.of(0, 10);
        var query = AuthoritySearchQueryDTO.builder()
                .pageable(pageable)
                .xApplicationUuid(UUID.randomUUID())
                .build();
        var view = new AuthoritySearchView(UUID.randomUUID(), "READ_USERS", "desc", LocalDateTime.now(), LocalDateTime.now());
        var page = new PageResultView<>(List.of(view), 0, 10, 1, 1, true, true, false, false);
        when(queryService.findAll(pageable)).thenReturn(page);

        var result = useCaseService.findAll(query);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(queryService).findAll(pageable);
    }
}

