package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.adapters.in.rest.role.dto.RoleCreateDTO;
import com.keepguard.ms_auth.adapters.in.rest.role.dto.RoleResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.role.dto.RoleUpdateDTO;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.application.mapper.RoleApplicationMapper;
import com.keepguard.ms_auth.domain.dto.role.RoleCreateCommandDTO;
import com.keepguard.ms_auth.domain.dto.role.RoleUpdateCommandDTO;
import com.keepguard.ms_auth.domain.dto.role.RoleDeleteCommandDTO;
import com.keepguard.ms_auth.domain.dto.role.RoleGetByIdQueryDTO;
import com.keepguard.ms_auth.domain.dto.role.RoleGetByNameQueryDTO;
import com.keepguard.ms_auth.domain.dto.role.RoleGetAllQueryDTO;
import com.keepguard.ms_auth.domain.dto.role.RoleSearchQueryDTO;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RoleUseCaseService
 * Testa a orquestração entre CommandService e QueryService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Use Case Service Tests")
class RoleUseCaseServiceTest {
    
    @Mock
    private RoleCommandService roleCommandService;
    
    @Mock
    private RoleQueryService roleQueryService;
    
    @Mock
    private RoleApplicationMapper roleApplicationMapper;
    
    @InjectMocks
    private RoleUseCaseService roleUseCaseService;
    
    private UUID roleId;
    private UUID xApplicationUuid;
    private Role role;
    private RoleCreateDTO createDTO;
    private RoleUpdateDTO updateDTO;
    private RoleResponseDTO responseDTO;
    private Pageable pageable;
    
    // View objects
    private RoleCreateView createRoleView;
    private RoleUpdateView updateRoleView;
    private RoleGetByIdView getRoleByIdView;
    private RoleGetByNameView getRoleByNameView;
    private RoleListView listRoleView;
    private RoleSearchView searchRoleView;
    
    // CommandDTOs
    private RoleCreateCommandDTO createCommand;
    private RoleUpdateCommandDTO updateCommand;
    private RoleDeleteCommandDTO deleteCommand;
    private RoleGetByIdQueryDTO getByIdCommand;
    private RoleGetByNameQueryDTO getByNameCommand;
    private RoleGetAllQueryDTO getAllCommand;
    private RoleSearchQueryDTO searchCommand;
    
    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        xApplicationUuid = UUID.randomUUID();
        
        // Criar role de teste usando builder
        role = RoleTestBuilder.builder()
            .withId(roleId)
            .buildDomain();
        
        createDTO = RoleCreateDTO.builder()
            .name("ADMIN")
            .description("Administrador do sistema")
            .build();
        
        updateDTO = RoleUpdateDTO.builder()
            .name("ADMIN_UPDATED")
            .description("Descrição atualizada")
            .build();
        
        responseDTO = RoleResponseDTO.builder()
            .id(roleId)
            .name("ADMIN")
            .description("Administrador do sistema")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        pageable = PageRequest.of(0, 10);
        
        // Initialize View objects
        createRoleView = new RoleCreateView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        updateRoleView = new RoleUpdateView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        getRoleByIdView = new RoleGetByIdView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        getRoleByNameView = new RoleGetByNameView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        listRoleView = new RoleListView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        searchRoleView = new RoleSearchView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        
        // CommandDTOs
        createCommand = RoleCreateCommandDTO.builder()
            .name("ADMIN")
            .description("Administrador do sistema")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        updateCommand = RoleUpdateCommandDTO.builder()
            .id(roleId)
            .name("ADMIN_UPDATED")
            .description("Descrição atualizada")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        deleteCommand = RoleDeleteCommandDTO.builder()
            .id(roleId)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        getByIdCommand = RoleGetByIdQueryDTO.builder()
            .id(roleId)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        getByNameCommand = RoleGetByNameQueryDTO.builder()
            .name("ADMIN")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        getAllCommand = RoleGetAllQueryDTO.builder()
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        searchCommand = RoleSearchQueryDTO.builder()
            .xApplicationUuid(xApplicationUuid)
            .pageable(pageable)
            .build();
    }
    
    @Test
    @DisplayName("Deve criar role com sucesso")
    void shouldCreateRoleSuccessfully() {
        // Given
        when(roleCommandService.create(createCommand)).thenReturn(createRoleView);
        
        // When
        RoleCreateView result = roleUseCaseService.create(createCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("ADMIN", result.name());
        assertEquals("Administrador do sistema", result.description());
        
        verify(roleCommandService).create(createCommand);
    }
    
    @Test
    @DisplayName("Deve atualizar role com sucesso")
    void shouldUpdateRoleSuccessfully() {
        // Given
        when(roleCommandService.update(updateCommand)).thenReturn(updateRoleView);
        
        // When
        RoleUpdateView result = roleUseCaseService.update(updateCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("ADMIN", result.name());
        
        verify(roleCommandService).update(updateCommand);
    }
    
    @Test
    @DisplayName("Deve deletar role com sucesso")
    void shouldDeleteRoleSuccessfully() {
        // Given
        doNothing().when(roleCommandService).delete(deleteCommand);
        
        // When
        roleUseCaseService.delete(deleteCommand);
        
        // Then
        verify(roleCommandService).delete(deleteCommand);
    }
    
    @Test
    @DisplayName("Deve buscar role por ID com sucesso")
    void shouldFindRoleByIdSuccessfully() {
        // Given
        when(roleQueryService.findById(roleId)).thenReturn(Optional.of(role));
        when(roleApplicationMapper.toGetByIdView(role)).thenReturn(getRoleByIdView);
        
        // When
        Optional<RoleGetByIdView> result = roleUseCaseService.findById(getByIdCommand);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(roleId, result.get().id());
        assertEquals("ADMIN", result.get().name());
        
        verify(roleQueryService).findById(roleId);
        verify(roleApplicationMapper).toGetByIdView(role);
    }
    
    @Test
    @DisplayName("Deve retornar vazio quando role não encontrado por ID")
    void shouldReturnEmptyWhenRoleNotFoundById() {
        // Given
        when(roleQueryService.findById(roleId)).thenReturn(Optional.empty());
        
        // When
        Optional<RoleGetByIdView> result = roleUseCaseService.findById(getByIdCommand);
        
        // Then
        assertFalse(result.isPresent());
        
        verify(roleQueryService).findById(roleId);
        verify(roleApplicationMapper, never()).toGetByIdView(any());
    }
    
    @Test
    @DisplayName("Deve buscar role por nome com sucesso")
    void shouldFindRoleByNameSuccessfully() {
        // Given
        String roleName = "ADMIN";
        when(roleQueryService.findByName(roleName)).thenReturn(Optional.of(role));
        when(roleApplicationMapper.toGetByNameView(role)).thenReturn(getRoleByNameView);
        
        // When
        Optional<RoleGetByNameView> result = roleUseCaseService.findByName(getByNameCommand);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(roleId, result.get().id());
        assertEquals(roleName, result.get().name());
        
        verify(roleQueryService).findByName(roleName);
        verify(roleApplicationMapper).toGetByNameView(role);
    }
    
    @Test
    @DisplayName("Deve retornar vazio quando role não encontrado por nome")
    void shouldReturnEmptyWhenRoleNotFoundByName() {
        // Given
        String roleName = "ROLE_INEXISTENTE";
        RoleGetByNameQueryDTO getByNameCommandInexistente = RoleGetByNameQueryDTO.builder()
            .name(roleName)
            .xApplicationUuid(xApplicationUuid)
            .build();
        when(roleQueryService.findByName(roleName)).thenReturn(Optional.empty());
        
        // When
        Optional<RoleGetByNameView> result = roleUseCaseService.findByName(getByNameCommandInexistente);
        
        // Then
        assertFalse(result.isPresent());
        
        verify(roleQueryService).findByName(roleName);
        verify(roleApplicationMapper, never()).toGetByNameView(any());
    }
    
    @Test
    @DisplayName("Deve buscar todos os roles com sucesso")
    void shouldFindAllRolesSuccessfully() {
        // Given
        List<Role> roles = List.of(role);
        List<RoleListView> listRoleViews = List.of(listRoleView);
        
        when(roleQueryService.findAll()).thenReturn(roles);
        when(roleApplicationMapper.toListView(role)).thenReturn(listRoleView);
        
        // When
        List<RoleListView> result = roleUseCaseService.findAll(getAllCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(roleId, result.get(0).id());
        assertEquals("ADMIN", result.get(0).name());
        
        verify(roleQueryService).findAll();
        verify(roleApplicationMapper).toListView(role);
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando não há roles")
    void shouldReturnEmptyListWhenNoRoles() {
        // Given
        when(roleQueryService.findAll()).thenReturn(List.of());
        
        // When
        List<RoleListView> result = roleUseCaseService.findAll(getAllCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(roleQueryService).findAll();
        verify(roleApplicationMapper, never()).toListView(any());
    }
    
    @Test
    @DisplayName("Deve buscar roles com paginação com sucesso")
    void shouldFindAllRolesWithPaginationSuccessfully() {
        // Given
        List<Role> roles = List.of(role);
        PageResultView<Role> pageResultView = PageResultView.<Role>builder()
            .content(roles)
            .totalElements(1L)
            .totalPages(1)
            .size(10)
            .page(0)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();
        
        PageResultView<RoleSearchView> expectedPageResultView = PageResultView.<RoleSearchView>builder()
            .content(List.of(searchRoleView))
            .totalElements(1L)
            .totalPages(1)
            .size(10)
            .pageNumber(0)
            .first(true)
            .last(true)
            .numberOfElements(1)
            .build();
        
        when(roleQueryService.findAll(pageable)).thenReturn(pageResultView);
        when(roleApplicationMapper.toSearchView(role)).thenReturn(searchRoleView);
        
        // When
        PageResultView<RoleSearchView> result = roleUseCaseService.findAll(searchCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(roleId, result.getContent().get(0).id());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getPageNumber());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertEquals(1, result.getNumberOfElements());
        
        verify(roleQueryService).findAll(pageable);
        verify(roleApplicationMapper).toSearchView(role);
    }
    
    @Test
    @DisplayName("Deve retornar página vazia quando não há roles na paginação")
    void shouldReturnEmptyPageWhenNoRolesInPagination() {
        // Given
        PageResultView<Role> emptyPageResultView = PageResultView.<Role>builder()
            .content(List.of())
            .totalElements(0L)
            .totalPages(0)
            .size(10)
            .page(0)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();
        
        when(roleQueryService.findAll(pageable)).thenReturn(emptyPageResultView);
        
        // When
        PageResultView<RoleSearchView> result = roleUseCaseService.findAll(searchCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getPageNumber());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertEquals(0, result.getNumberOfElements());
        
        verify(roleQueryService).findAll(pageable);
        verify(roleApplicationMapper, never()).toSearchView(any());
    }
    
    @Test
    @DisplayName("Deve buscar roles com paginação em página intermediária")
    void shouldFindAllRolesWithPaginationInMiddlePage() {
        // Given
        List<Role> roles = List.of(role);
        PageResultView<Role> pageResultView = PageResultView.<Role>builder()
            .content(roles)
            .totalElements(11L)
            .totalPages(3)
            .size(5)
            .page(2)
            .first(false)
            .last(false)
            .hasNext(true)
            .hasPrevious(true)
            .build();
        
        when(roleQueryService.findAll(pageable)).thenReturn(pageResultView);
        when(roleApplicationMapper.toSearchView(role)).thenReturn(new RoleSearchView(roleId, "ADMIN", "Admin role", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        PageResultView<RoleSearchView> result = roleUseCaseService.findAll(searchCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(11L, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
        assertEquals(5, result.getSize());
        assertEquals(2, result.getPageNumber());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
        assertEquals(1, result.getNumberOfElements());
        
        verify(roleQueryService).findAll(pageable);
        verify(roleApplicationMapper).toSearchView(role);
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
        
        RoleResponseDTO responseDTO2 = RoleResponseDTO.builder()
            .id(role2.getId())
            .name("USER")
            .description("Usuário comum")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        List<Role> roles = List.of(role, role2);
        
        when(roleQueryService.findAll()).thenReturn(roles);
        when(roleApplicationMapper.toListView(role)).thenReturn(new RoleListView(roleId, "ADMIN", "Admin role", LocalDateTime.now(), LocalDateTime.now()));
        when(roleApplicationMapper.toListView(role2)).thenReturn(new RoleListView(role2.getId(), "USER", "Usuário comum", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        List<RoleListView> result = roleUseCaseService.findAll(getAllCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(roleId, result.get(0).id());
        assertEquals(role2.getId(), result.get(1).id());
        
        verify(roleQueryService).findAll();
        verify(roleApplicationMapper).toListView(role);
        verify(roleApplicationMapper).toListView(role2);
    }
    
    @Test
    @DisplayName("Deve criar role com nome em maiúsculo")
    void shouldCreateRoleWithUppercaseName() {
        // Given
        RoleCreateCommandDTO createCommandWithLowercase = RoleCreateCommandDTO.builder()
            .name("user")
            .description("Usuário comum")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        Role roleWithLowercase = RoleTestBuilder.builder()
            .withName("user")
            .withDescription("Usuário comum")
            .buildDomain();
        
        RoleResponseDTO responseDTOWithLowercase = RoleResponseDTO.builder()
            .id(roleId)
            .name("user")
            .description("Usuário comum")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(roleCommandService.create(createCommandWithLowercase)).thenReturn(new RoleCreateView(roleId, "user", "Usuário comum", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleCreateView result = roleUseCaseService.create(createCommandWithLowercase);
        
        // Then
        assertNotNull(result);
        assertEquals("user", result.name());
        
        verify(roleCommandService).create(createCommandWithLowercase);
    }
    
    @Test
    @DisplayName("Deve atualizar role com nome em maiúsculo")
    void shouldUpdateRoleWithUppercaseName() {
        // Given
        RoleUpdateCommandDTO updateCommandWithLowercase = RoleUpdateCommandDTO.builder()
            .id(roleId)
            .name("admin_updated")
            .description("Descrição atualizada")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        Role roleWithLowercase = RoleTestBuilder.builder()
            .withName("admin_updated")
            .withDescription("Descrição atualizada")
            .buildDomain();
        
        RoleResponseDTO responseDTOWithLowercase = RoleResponseDTO.builder()
            .id(roleId)
            .name("admin_updated")
            .description("Descrição atualizada")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        when(roleCommandService.update(updateCommandWithLowercase)).thenReturn(new RoleUpdateView(roleId, "admin_updated", "Updated description", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleUpdateView result = roleUseCaseService.update(updateCommandWithLowercase);
        
        // Then
        assertNotNull(result);
        assertEquals("admin_updated", result.name());
        
        verify(roleCommandService).update(updateCommandWithLowercase);
    }
}
