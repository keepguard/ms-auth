package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.RoleJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.RoleSpringRepository;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Role Repository Adapter Tests")
class RoleRepositoryAdapterTest {

    @Mock private RoleSpringRepository jpaRepository;
    @Mock private RoleJpaMapper roleJpaMapper;

    @InjectMocks private RoleRepositoryAdapter adapter;

    private Role role;
    private RoleJpaEntity roleJpaEntity;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        role = RoleTestBuilder.builder()
            .withId(roleId)
            .buildDomain();
            
        roleJpaEntity = RoleJpaEntity.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }

    @Test
    @DisplayName("Deve salvar role com sucesso")
    void shouldSaveRoleSuccessfully() {
        // Given
        when(roleJpaMapper.toJpaEntity(role)).thenReturn(roleJpaEntity);
        when(jpaRepository.save(roleJpaEntity)).thenReturn(roleJpaEntity);
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        Role result = adapter.save(role);

        // Then
        assertNotNull(result);
        assertEquals(role, result);
        verify(roleJpaMapper, times(1)).toJpaEntity(role);
        verify(jpaRepository, times(1)).save(roleJpaEntity);
        verify(roleJpaMapper, times(1)).toDomain(roleJpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar role por ID com sucesso")
    void shouldFindRoleByIdSuccessfully() {
        // Given
        when(jpaRepository.findById(roleId)).thenReturn(Optional.of(roleJpaEntity));
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        Optional<Role> result = adapter.findById(roleId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(role, result.get());
        verify(jpaRepository, times(1)).findById(roleId);
        verify(roleJpaMapper, times(1)).toDomain(roleJpaEntity);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando role não encontrada por ID")
    void shouldReturnEmptyOptionalWhenRoleNotFoundById() {
        // Given
        when(jpaRepository.findById(roleId)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = adapter.findById(roleId);

        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository).findById(roleId);
    }

    @Test
    @DisplayName("Deve buscar todas as roles com sucesso")
    void shouldFindAllRolesSuccessfully() {
        // Given
        List<RoleJpaEntity> roleJpaEntities = List.of(roleJpaEntity);
        when(jpaRepository.findAll()).thenReturn(roleJpaEntities);
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        List<Role> result = adapter.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(role, result.get(0));
        verify(jpaRepository, times(1)).findAll();
        verify(roleJpaMapper, times(1)).toDomain(roleJpaEntity);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há roles")
    void shouldReturnEmptyListWhenNoRoles() {
        // Given
        when(jpaRepository.findAll()).thenReturn(List.of());

        // When
        List<Role> result = adapter.findAll();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar roles com paginação com sucesso")
    void shouldFindAllRolesWithPaginationSuccessfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<RoleJpaEntity> roleJpaPage = new PageImpl<>(List.of(roleJpaEntity));
        when(jpaRepository.findAll(pageable)).thenReturn(roleJpaPage);
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        Page<Role> result = adapter.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(role, result.getContent().get(0));
        assertEquals(0, result.getNumber());
        assertEquals(1, result.getSize());
        verify(jpaRepository, times(1)).findAll(pageable);
        verify(roleJpaMapper, times(1)).toDomain(roleJpaEntity);
    }

    @Test
    @DisplayName("Deve deletar role por ID com sucesso")
    void shouldDeleteRoleByIdSuccessfully() {
        // When
        adapter.deleteById(roleId);

        // Then
        verify(jpaRepository).deleteById(roleId);
    }

    @Test
    @DisplayName("Deve deletar role com sucesso")
    void shouldDeleteRoleSuccessfully() {
        // Given
        when(roleJpaMapper.toJpaEntity(role)).thenReturn(roleJpaEntity);
        doNothing().when(jpaRepository).delete(roleJpaEntity);

        // When
        adapter.delete(role);

        // Then
        verify(roleJpaMapper, times(1)).toJpaEntity(role);
        verify(jpaRepository, times(1)).delete(roleJpaEntity);
    }

    @Test
    @DisplayName("Deve encontrar role por nome com sucesso")
    void shouldFindRoleByNameSuccessfully() {
        // Given
        String roleName = "ADMIN";
        when(jpaRepository.findByName(roleName)).thenReturn(Optional.of(roleJpaEntity));
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        Optional<Role> result = adapter.findByName(roleName);

        // Then
        assertTrue(result.isPresent());
        assertEquals(role, result.get());
        verify(jpaRepository, times(1)).findByName(roleName);
        verify(roleJpaMapper, times(1)).toDomain(roleJpaEntity);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando role não encontrada por nome")
    void shouldReturnEmptyOptionalWhenRoleNotFoundByName() {
        // Given
        String roleName = "ROLE_INEXISTENTE";
        when(jpaRepository.findByName(roleName)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = adapter.findByName(roleName);

        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository).findByName(roleName);
    }

    @Test
    @DisplayName("Deve buscar roles com paginação na primeira página")
    void shouldFindAllRolesWithPaginationFirstPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Page<RoleJpaEntity> roleJpaPage = new PageImpl<>(List.of(roleJpaEntity), pageable, 1L);
        when(jpaRepository.findAll(pageable)).thenReturn(roleJpaPage);
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        Page<Role> result = adapter.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(1L, result.getTotalElements());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    @DisplayName("Deve buscar roles com paginação na última página")
    void shouldFindAllRolesWithPaginationLastPage() {
        // Given
        Pageable pageable = PageRequest.of(1, 5);
        Page<RoleJpaEntity> roleJpaPage = new PageImpl<>(List.of(roleJpaEntity), pageable, 6L);
        when(jpaRepository.findAll(pageable)).thenReturn(roleJpaPage);
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        Page<Role> result = adapter.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(6L, result.getTotalElements());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertTrue(result.hasPrevious());
    }

    @Test
    @DisplayName("Deve buscar roles com paginação na página do meio")
    void shouldFindAllRolesWithPaginationMiddlePage() {
        // Given
        Pageable pageable = PageRequest.of(1, 5);
        Page<RoleJpaEntity> roleJpaPage = new PageImpl<>(List.of(roleJpaEntity), pageable, 11L);
        when(jpaRepository.findAll(pageable)).thenReturn(roleJpaPage);
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);

        // When
        Page<Role> result = adapter.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());
        assertEquals(11L, result.getTotalElements());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
        assertTrue(result.hasNext());
        assertTrue(result.hasPrevious());
    }

    @Test
    @DisplayName("Deve buscar múltiplas roles com sucesso")
    void shouldFindMultipleRolesSuccessfully() {
        // Given
        Role role2 = RoleTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withName("USER")
            .buildDomain();
        RoleJpaEntity roleJpaEntity2 = RoleJpaEntity.builder()
            .id(role2.getId())
            .name(role2.getName())
            .description(role2.getDescription())
            .createdAt(role2.getCreatedAt())
            .updatedAt(role2.getUpdatedAt())
            .build();
        List<RoleJpaEntity> roleJpaEntities = List.of(roleJpaEntity, roleJpaEntity2);
        when(jpaRepository.findAll()).thenReturn(roleJpaEntities);
        when(roleJpaMapper.toDomain(roleJpaEntity)).thenReturn(role);
        when(roleJpaMapper.toDomain(roleJpaEntity2)).thenReturn(role2);

        // When
        List<Role> result = adapter.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(role));
        assertTrue(result.contains(role2));
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar roles com diferentes nomes")
    void shouldFindRolesWithDifferentNames() {
        // Given
        String[] roleNames = {"ADMIN", "USER", "MANAGER", "GUEST"};
        
        for (String roleName : roleNames) {
            Role testRole = RoleTestBuilder.builder()
                .withName(roleName)
                .buildDomain();
            RoleJpaEntity testRoleJpaEntity = RoleJpaEntity.builder()
                .id(testRole.getId())
                .name(testRole.getName())
                .description(testRole.getDescription())
                .createdAt(testRole.getCreatedAt())
                .updatedAt(testRole.getUpdatedAt())
                .build();
            when(jpaRepository.findByName(roleName)).thenReturn(Optional.of(testRoleJpaEntity));
            when(roleJpaMapper.toDomain(testRoleJpaEntity)).thenReturn(testRole);

            // When
            Optional<Role> result = adapter.findByName(roleName);

            // Then
            assertTrue(result.isPresent());
            assertEquals(roleName, result.get().getName());
            verify(jpaRepository).findByName(roleName);
        }
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para role inexistente")
    void shouldReturnEmptyOptionalForNonExistentRole() {
        // Given
        String nonExistentRole = "NON_EXISTENT";
        when(jpaRepository.findByName(nonExistentRole)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = adapter.findByName(nonExistentRole);

        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository).findByName(nonExistentRole);
    }
}
