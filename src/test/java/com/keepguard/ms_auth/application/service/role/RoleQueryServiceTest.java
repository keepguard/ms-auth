package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.mapper.RoleApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.domain.entity.role.Role;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RoleQueryService
 * Cobertura: 100% dos métodos públicos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Query Service Tests")
class RoleQueryServiceTest {
    
    @Mock
    private RoleRepositoryPort roleRepository;
    
    @Mock
    private RoleApplicationMapper roleApplicationMapper;
    
    @InjectMocks
    private RoleQueryService roleQueryService;
    
    private UUID roleId;
    private Role role;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        
        // Criar role de teste usando builder
        role = RoleTestBuilder.builder()
            .withId(roleId)
            .buildDomain();
        
        pageable = PageRequest.of(0, 10);
    }
    
    @Test
    @DisplayName("Deve buscar role por ID com sucesso")
    void shouldFindRoleByIdSuccessfully() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        
        // When
        Optional<Role> result = roleQueryService.findById(roleId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(role, result.get());
        assertEquals(roleId, result.get().getId());
        
        verify(roleRepository).findById(roleId);
    }
    
    @Test
    @DisplayName("Deve retornar vazio quando role não encontrado por ID")
    void shouldReturnEmptyWhenRoleNotFoundById() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());
        
        // When
        Optional<Role> result = roleQueryService.findById(roleId);
        
        // Then
        assertFalse(result.isPresent());
        
        verify(roleRepository).findById(roleId);
    }
    
    @Test
    @DisplayName("Deve buscar role por nome com sucesso")
    void shouldFindRoleByNameSuccessfully() {
        // Given
        String roleName = "ADMIN";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role));
        
        // When
        Optional<Role> result = roleQueryService.findByName(roleName);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(role, result.get());
        assertEquals(roleName, result.get().getName());
        
        verify(roleRepository).findByName(roleName);
    }
    
    @Test
    @DisplayName("Deve retornar vazio quando role não encontrado por nome")
    void shouldReturnEmptyWhenRoleNotFoundByName() {
        // Given
        String roleName = "ROLE_INEXISTENTE";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.empty());
        
        // When
        Optional<Role> result = roleQueryService.findByName(roleName);
        
        // Then
        assertFalse(result.isPresent());
        
        verify(roleRepository).findByName(roleName);
    }
    
    @Test
    @DisplayName("Deve buscar todos os roles com sucesso")
    void shouldFindAllRolesSuccessfully() {
        // Given
        List<Role> roles = List.of(role);
        when(roleRepository.findAll()).thenReturn(roles);
        
        // When
        List<Role> result = roleQueryService.findAll();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(role, result.get(0));
        
        verify(roleRepository).findAll();
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando não há roles")
    void shouldReturnEmptyListWhenNoRoles() {
        // Given
        when(roleRepository.findAll()).thenReturn(List.of());
        
        // When
        List<Role> result = roleQueryService.findAll();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(roleRepository).findAll();
    }
    
    @Test
    @DisplayName("Deve buscar roles com paginação com sucesso")
    void shouldFindAllRolesWithPaginationSuccessfully() {
        // Given
        List<Role> roles = List.of(role);
        Page<Role> page = new PageImpl<>(roles, pageable, 1L);
        
        when(roleRepository.findAll(pageable)).thenReturn(page);
        
        // When
        PageResultView<Role> result = roleQueryService.findAll(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(role, result.getContent().get(0));
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getPageNumber());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
        
        verify(roleRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("Deve retornar página vazia quando não há roles na paginação")
    void shouldReturnEmptyPageWhenNoRolesInPagination() {
        // Given
        Page<Role> emptyPage = new PageImpl<>(List.of(), pageable, 0L);
        
        when(roleRepository.findAll(pageable)).thenReturn(emptyPage);
        
        // When
        PageResultView<Role> result = roleQueryService.findAll(pageable);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getPageNumber());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
        
        verify(roleRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("Deve buscar roles com paginação em página intermediária")
    void shouldFindAllRolesWithPaginationInMiddlePage() {
        // Given
        List<Role> roles = List.of(role);
        Pageable middlePageable = PageRequest.of(2, 5); // Página 2, tamanho 5
        Page<Role> page = new PageImpl<>(roles, middlePageable, 11L); // Total 11 elementos
        
        when(roleRepository.findAll(middlePageable)).thenReturn(page);
        
        // When
        PageResultView<Role> result = roleQueryService.findAll(middlePageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(11L, result.getTotalElements());
        assertEquals(3, result.getTotalPages()); // 11 elementos / 5 por página = 3 páginas
        assertEquals(5, result.getSize());
        assertEquals(2, result.getPageNumber());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertTrue(result.hasPrevious());
        
        verify(roleRepository).findAll(middlePageable);
    }
    
    @Test
    @DisplayName("Deve buscar roles com paginação na última página")
    void shouldFindAllRolesWithPaginationInLastPage() {
        // Given
        List<Role> roles = List.of(role);
        Pageable lastPageable = PageRequest.of(2, 5); // Página 2, tamanho 5
        Page<Role> page = new PageImpl<>(roles, lastPageable, 11L); // Total 11 elementos
        
        when(roleRepository.findAll(lastPageable)).thenReturn(page);
        
        // When
        PageResultView<Role> result = roleQueryService.findAll(lastPageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(11L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(5, result.getSize());
        assertEquals(2, result.getPageNumber());
        assertFalse(result.isFirst());
        assertTrue(result.isLast()); // Última página
        assertFalse(result.hasNext());
        assertTrue(result.hasPrevious());
        
        verify(roleRepository).findAll(lastPageable);
    }
    
    @Test
    @DisplayName("Deve buscar roles com paginação na primeira página")
    void shouldFindAllRolesWithPaginationInFirstPage() {
        // Given
        List<Role> roles = List.of(role);
        Pageable firstPageable = PageRequest.of(0, 5); // Página 0, tamanho 5
        Page<Role> page = new PageImpl<>(roles, firstPageable, 11L); // Total 11 elementos
        
        when(roleRepository.findAll(firstPageable)).thenReturn(page);
        
        // When
        PageResultView<Role> result = roleQueryService.findAll(firstPageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(11L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(5, result.getSize());
        assertEquals(0, result.getPageNumber());
        assertTrue(result.isFirst()); // Primeira página
        assertFalse(result.isLast());
        assertTrue(result.hasNext());
        assertFalse(result.hasPrevious());
        
        verify(roleRepository).findAll(firstPageable);
    }
    
    @Test
    @DisplayName("Deve buscar roles com diferentes tamanhos de página")
    void shouldFindAllRolesWithDifferentPageSizes() {
        // Given
        List<Role> roles = List.of(role);
        Pageable smallPageable = PageRequest.of(0, 1); // Tamanho 1
        Page<Role> page = new PageImpl<>(roles, smallPageable, 1L);
        
        when(roleRepository.findAll(smallPageable)).thenReturn(page);
        
        // When
        PageResultView<Role> result = roleQueryService.findAll(smallPageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getPageNumber());
        
        verify(roleRepository).findAll(smallPageable);
    }
    
    @Test
    @DisplayName("Deve buscar roles com múltiplos roles")
    void shouldFindAllRolesWithMultipleRoles() {
        // Given
        Role role2 = RoleTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withName("USER")
            .withDescription("Usuário comum")
            .buildDomain();
        
        List<Role> roles = List.of(role, role2);
        
        when(roleRepository.findAll()).thenReturn(roles);
        
        // When
        List<Role> result = roleQueryService.findAll();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(roleId, result.get(0).getId());
        assertEquals(role2.getId(), result.get(1).getId());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
        
        verify(roleRepository).findAll();
    }
}
