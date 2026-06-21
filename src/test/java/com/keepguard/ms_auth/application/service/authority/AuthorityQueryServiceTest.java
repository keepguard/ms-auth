package com.keepguard.ms_auth.application.service.authority;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.mapper.AuthorityApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthorityQueryService Tests")
class AuthorityQueryServiceTest {

    private AuthorityQueryService queryService;
    private AuthorityRepositoryPort repository;
    private AuthorityApplicationMapper mapper;

    @BeforeEach
    void setUp() {
        repository = mock(AuthorityRepositoryPort.class);
        mapper = mock(AuthorityApplicationMapper.class);
        queryService = new AuthorityQueryService(repository, mapper);
    }

    @Test
    @DisplayName("findById retorna authority quando existe")
    void findById_shouldReturnAuthorityWhenExists() {
        UUID id = UUID.randomUUID();
        var authority = Authority.builder()
                .id(id)
                .name("READ_USERS")
                .description("desc")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        var view = new AuthorityGetByIdView(id, "READ_USERS", "desc", authority.getCreatedAt(), authority.getUpdatedAt());
        when(repository.findById(id)).thenReturn(Optional.of(authority));
        when(mapper.toGetByIdView(authority)).thenReturn(view);

        var result = queryService.findById(id);

        assertTrue(result.isPresent());
        assertEquals("READ_USERS", result.get().name());
        verify(repository).findById(id);
        verify(mapper).toGetByIdView(authority);
    }

    @Test
    @DisplayName("findById retorna vazio quando não existe")
    void findById_shouldReturnEmptyWhenNotExists() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        var result = queryService.findById(id);

        assertFalse(result.isPresent());
        verify(repository).findById(id);
        verify(mapper, never()).toGetByIdView(any());
    }

    @Test
    @DisplayName("findByName retorna authority quando existe")
    void findByName_shouldReturnAuthorityWhenExists() {
        String name = "READ_USERS";
        var authority = Authority.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("desc")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        var view = new AuthorityGetByNameView(authority.getId(), name, "desc", authority.getCreatedAt(), authority.getUpdatedAt());
        when(repository.findByName(name)).thenReturn(Optional.of(authority));
        when(mapper.toGetByNameView(authority)).thenReturn(view);

        var result = queryService.findByName(name);

        assertTrue(result.isPresent());
        assertEquals(name, result.get().name());
        verify(repository).findByName(name);
        verify(mapper).toGetByNameView(authority);
    }

    @Test
    @DisplayName("findByName retorna vazio quando não existe")
    void findByName_shouldReturnEmptyWhenNotExists() {
        String name = "NONEXISTENT";
        when(repository.findByName(name)).thenReturn(Optional.empty());

        var result = queryService.findByName(name);

        assertFalse(result.isPresent());
        verify(repository).findByName(name);
        verify(mapper, never()).toGetByNameView(any());
    }

    @Test
    @DisplayName("findAll retorna lista de authorities")
    void findAll_shouldReturnListOfAuthorities() {
        var auth1 = Authority.builder()
                .id(UUID.randomUUID())
                .name("READ_USERS")
                .description("desc1")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        var auth2 = Authority.builder()
                .id(UUID.randomUUID())
                .name("WRITE_USERS")
                .description("desc2")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        var view1 = new AuthorityListView(auth1.getId(), "READ_USERS", "desc1", auth1.getCreatedAt(), auth1.getUpdatedAt());
        var view2 = new AuthorityListView(auth2.getId(), "WRITE_USERS", "desc2", auth2.getCreatedAt(), auth2.getUpdatedAt());
        when(repository.findAll()).thenReturn(List.of(auth1, auth2));
        when(mapper.toListView(auth1)).thenReturn(view1);
        when(mapper.toListView(auth2)).thenReturn(view2);

        var result = queryService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("READ_USERS", result.get(0).name());
        assertEquals("WRITE_USERS", result.get(1).name());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findAll com paginação retorna PageResultView")
    void findAllPaginated_shouldReturnPageResultView() {
        Pageable pageable = PageRequest.of(0, 10);
        var auth = Authority.builder()
                .id(UUID.randomUUID())
                .name("READ_USERS")
                .description("desc")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Page<Authority> page = new PageImpl<>(List.of(auth), pageable, 1);
        var view = new AuthoritySearchView(auth.getId(), "READ_USERS", "desc", auth.getCreatedAt(), auth.getUpdatedAt());
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toSearchView(auth)).thenReturn(view);

        var result = queryService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals("READ_USERS", result.getContent().get(0).name());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("findAll retorna lista vazia quando não há authorities")
    void findAll_shouldReturnEmptyListWhenNoAuthorities() {
        when(repository.findAll()).thenReturn(List.of());

        var result = queryService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("findAll com paginação retorna página vazia quando não há authorities")
    void findAllPaginated_shouldReturnEmptyPageWhenNoAuthorities() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Authority> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findAll(pageable)).thenReturn(emptyPage);

        var result = queryService.findAll(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(repository).findAll(pageable);
    }
}

