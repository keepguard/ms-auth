package com.keepguard.ms_auth.adapters.in.rest.role;

import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.adapters.in.rest.role.dto.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.adapters.in.rest.role.mapper.RoleAdapterMapper;
import com.keepguard.ms_auth.application.port.in.RolePort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.dto.role.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

/**
 * Testes unitários para RoleController
 * Testa lógica do controller sem contexto Spring
 */
@DisplayName("Role Controller Tests")
class RoleControllerTest {
    
    @Mock
    private RolePort rolePort;
    
    @Mock
    private RoleAdapterMapper mapper;
    
    private RoleController roleController;
    
    private UUID roleId;
    private UUID xApplicationUuid;
    private String xApplication;
    private RoleResponseDTO roleResponseDTO;
    private RoleCreateDTO roleCreateDTO;
    private RoleUpdateDTO roleUpdateDTO;
    private PageResultView<RoleResponseDTO> pageResultView;
    
    // View objects
    private RoleCreateView createRoleView;
    private RoleUpdateView updateRoleView;
    private RoleGetByIdView getRoleByIdView;
    private RoleGetByNameView getRoleByNameView;
    private RoleListView listRoleView;
    private RoleSearchView searchRoleView;
    private PageResultView<RoleSearchView> searchPageResultView;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        roleController = new RoleController(rolePort, mapper);
        
        roleId = UUID.randomUUID();
        xApplicationUuid = UUID.randomUUID();
        xApplication = xApplicationUuid.toString();
        
        roleResponseDTO = RoleResponseDTO.builder()
            .id(roleId)
            .name("ADMIN")
            .description("Administrador do sistema")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        roleCreateDTO = RoleCreateDTO.builder()
            .name("USER")
            .description("Usuário comum")
            .build();
        
        roleUpdateDTO = RoleUpdateDTO.builder()
            .name("ADMIN_UPDATED")
            .description("Administrador atualizado")
            .build();
        
        pageResultView = new PageResultView<>(
            List.of(roleResponseDTO),
            0,
            10,
            1L,
            1,
            true,
            true,
            false,
            false
        );
        
        // Initialize View objects
        createRoleView = new RoleCreateView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        updateRoleView = new RoleUpdateView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        getRoleByIdView = new RoleGetByIdView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        getRoleByNameView = new RoleGetByNameView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        listRoleView = new RoleListView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        searchRoleView = new RoleSearchView(roleId, "ADMIN", "Administrador do sistema", LocalDateTime.now(), LocalDateTime.now());
        searchPageResultView = new PageResultView<>(
            List.of(searchRoleView),
            0,
            10,
            1L,
            1,
            true,
            true,
            false,
            false
        );
    }
    
    @Test
    @DisplayName("Deve criar role com sucesso")
    void shouldCreateRoleSuccessfully() {
        // Given
        var command = RoleCreateCommandDTO.builder()
            .name("ADMIN")
            .description("Administrador do sistema")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toCreateCommand(any(RoleCreateDTO.class), any(UUID.class))).thenReturn(command);
            when(rolePort.create(any(RoleCreateCommandDTO.class))).thenReturn(createRoleView);
            when(mapper.toCreateResponseDTO(any(RoleCreateView.class))).thenReturn(RoleCreateResponseDTO.builder()
                .id(roleId)
                .name("ADMIN")
                .description("Administrador do sistema")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<RoleCreateResponseDTO> response = roleController.create(xApplication, roleCreateDTO);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(roleId, response.getBody().getId());
            assertEquals("ADMIN", response.getBody().getName());
            assertEquals("Administrador do sistema", response.getBody().getDescription());
            
            verify(rolePort).create(command);
        }
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar role com dados inválidos")
    void shouldThrowExceptionWhenCreatingRoleWithInvalidData() {
        // Given
        var command = RoleCreateCommandDTO.builder()
            .name("ADMIN")
            .description("Administrador do sistema")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toCreateCommand(any(RoleCreateDTO.class), any(UUID.class))).thenReturn(command);
            when(rolePort.create(any(RoleCreateCommandDTO.class)))
                .thenThrow(new AlreadyExistsException("Role já existe"));
            
            // When & Then
            assertThrows(AlreadyExistsException.class, () -> {
                roleController.create(xApplication, roleCreateDTO);
            });
            
            verify(rolePort).create(command);
        }
    }
    
    @Test
    @DisplayName("Deve atualizar role com sucesso")
    void shouldUpdateRoleSuccessfully() {
        // Given
        var command = RoleUpdateCommandDTO.builder()
            .id(roleId)
            .name("ADMIN")
            .description("Administrador atualizado")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toUpdateCommand(any(UUID.class), any(RoleUpdateDTO.class), any(UUID.class))).thenReturn(command);
            when(rolePort.update(any(RoleUpdateCommandDTO.class)))
                .thenReturn(updateRoleView);
            when(mapper.toUpdateResponseDTO(any(RoleUpdateView.class))).thenReturn(RoleUpdateResponseDTO.builder()
                .id(roleId)
                .name("ADMIN")
                .description("Administrador atualizado")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<RoleUpdateResponseDTO> response = roleController.update(xApplication, roleId, roleUpdateDTO);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(roleId, response.getBody().getId());
            
            verify(rolePort).update(command);
        }
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao atualizar role inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentRole() {
        // Given
        var command = RoleUpdateCommandDTO.builder()
            .id(roleId)
            .name("ADMIN")
            .description("Administrador atualizado")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toUpdateCommand(any(UUID.class), any(RoleUpdateDTO.class), any(UUID.class))).thenReturn(command);
            when(rolePort.update(any(RoleUpdateCommandDTO.class)))
                .thenThrow(new NotFoundException("Role não encontrado"));
            
            // When & Then
            assertThrows(NotFoundException.class, () -> {
                roleController.update(xApplication, roleId, roleUpdateDTO);
            });
            
            verify(rolePort).update(command);
        }
    }
    
    @Test
    @DisplayName("Deve deletar role com sucesso")
    void shouldDeleteRoleSuccessfully() {
        // Given
        var command = RoleDeleteCommandDTO.builder()
            .id(roleId)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toDeleteCommand(any(UUID.class), any(UUID.class))).thenReturn(command);
            doNothing().when(rolePort).delete(any(RoleDeleteCommandDTO.class));
            
            // When
            ResponseEntity<Void> response = roleController.delete(xApplication, roleId);
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            
            verify(rolePort).delete(command);
        }
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao deletar role inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentRole() {
        // Given
        var command = RoleDeleteCommandDTO.builder()
            .id(roleId)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toDeleteCommand(any(UUID.class), any(UUID.class))).thenReturn(command);
            doThrow(new NotFoundException("Role não encontrado"))
                .when(rolePort).delete(any(RoleDeleteCommandDTO.class));
            
            // When & Then
            assertThrows(NotFoundException.class, () -> {
                roleController.delete(xApplication, roleId);
            });
            
            verify(rolePort).delete(command);
        }
    }
    
    @Test
    @DisplayName("Deve buscar role por ID com sucesso")
    void shouldGetRoleByIdSuccessfully() {
        // Given
        var command = RoleGetByIdQueryDTO.builder()
            .id(roleId)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toGetByIdCommand(any(UUID.class), any(UUID.class))).thenReturn(command);
            when(rolePort.findById(any(RoleGetByIdQueryDTO.class))).thenReturn(Optional.of(getRoleByIdView));
            when(mapper.toGetByIdResponseDTO(any(RoleGetByIdView.class))).thenReturn(RoleGetByIdResponseDTO.builder()
                .id(roleId)
                .name("ADMIN")
                .description("Administrador do sistema")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<RoleGetByIdResponseDTO> response = roleController.getById(xApplication, roleId);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(roleId, response.getBody().getId());
            assertEquals("ADMIN", response.getBody().getName());
            
            verify(rolePort).findById(command);
        }
    }
    
    @Test
    @DisplayName("Deve retornar 404 quando role não encontrado por ID")
    void shouldReturn404WhenRoleNotFoundById() {
        // Given
        var command = RoleGetByIdQueryDTO.builder()
            .id(roleId)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toGetByIdCommand(any(UUID.class), any(UUID.class))).thenReturn(command);
            when(rolePort.findById(any(RoleGetByIdQueryDTO.class))).thenReturn(Optional.empty());
            
            // When
            ResponseEntity<RoleGetByIdResponseDTO> response = roleController.getById(xApplication, roleId);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
            
            verify(rolePort).findById(command);
        }
    }
    
    @Test
    @DisplayName("Deve buscar role por nome com sucesso")
    void shouldGetRoleByNameSuccessfully() {
        // Given
        String roleName = "ADMIN";
        var command = RoleGetByNameQueryDTO.builder()
            .name(roleName)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toGetByNameCommand(any(String.class), any(UUID.class))).thenReturn(command);
            when(rolePort.findByName(any(RoleGetByNameQueryDTO.class))).thenReturn(Optional.of(getRoleByNameView));
            when(mapper.toGetByNameResponseDTO(any(RoleGetByNameView.class))).thenReturn(RoleGetByNameResponseDTO.builder()
                .id(roleId)
                .name(roleName)
                .description("Administrador do sistema")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<RoleGetByNameResponseDTO> response = roleController.getByName(xApplication, roleName);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(roleId, response.getBody().getId());
            assertEquals(roleName, response.getBody().getName());
            
            verify(rolePort).findByName(command);
        }
    }
    
    @Test
    @DisplayName("Deve retornar 404 quando role não encontrado por nome")
    void shouldReturn404WhenRoleNotFoundByName() {
        // Given
        String roleName = "INEXISTENTE";
        var command = RoleGetByNameQueryDTO.builder()
            .name(roleName)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toGetByNameCommand(any(String.class), any(UUID.class))).thenReturn(command);
            when(rolePort.findByName(any(RoleGetByNameQueryDTO.class))).thenReturn(Optional.empty());
            
            // When
            ResponseEntity<RoleGetByNameResponseDTO> response = roleController.getByName(xApplication, roleName);
            
            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNull(response.getBody());
            
            verify(rolePort).findByName(command);
        }
    }
    
    @Test
    @DisplayName("Deve listar todos os roles com sucesso")
    void shouldListAllRolesSuccessfully() {
        // Given
        List<RoleListView> roles = List.of(listRoleView);
        var command = RoleGetAllQueryDTO.builder()
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toGetAllCommand(any(UUID.class))).thenReturn(command);
            when(rolePort.findAll(any(RoleGetAllQueryDTO.class))).thenReturn(roles);
            when(mapper.toListResponseDTO(any(RoleListView.class))).thenReturn(RoleListResponseDTO.builder()
                .id(roleId)
                .name("ADMIN")
                .description("Administrador do sistema")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<List<RoleListResponseDTO>> response = roleController.listAll(xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
            assertEquals(roleId, response.getBody().get(0).getId());
            
            verify(rolePort).findAll(command);
        }
    }
    
    @Test
    @DisplayName("Deve buscar roles com paginação com sucesso")
    void shouldSearchRolesWithPaginationSuccessfully() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        var command = RoleSearchQueryDTO.builder()
            .pageable(pageable)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toSearchCommand(any(Pageable.class), any(UUID.class))).thenReturn(command);
            when(rolePort.findAll(any(RoleSearchQueryDTO.class))).thenReturn(searchPageResultView);
            when(mapper.toSearchResponseDTO(any(RoleSearchView.class))).thenReturn(RoleSearchResponseDTO.builder()
                .id(roleId)
                .name("ADMIN")
                .description("Administrador do sistema")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<PageResultView<RoleSearchResponseDTO>> response = roleController.search(xApplication, pageable);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            assertEquals(1L, response.getBody().getTotalElements());
            assertEquals(0, response.getBody().getPageNumber());
            assertEquals(10, response.getBody().getSize());
            
            verify(rolePort).findAll(command);
        }
    }
    
    @Test
    @DisplayName("Deve retornar lista vazia quando não há roles")
    void shouldReturnEmptyListWhenNoRoles() {
        // Given
        var command = RoleGetAllQueryDTO.builder()
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toGetAllCommand(any(UUID.class))).thenReturn(command);
            when(rolePort.findAll(any(RoleGetAllQueryDTO.class))).thenReturn(List.of());
            
            // When
            ResponseEntity<List<RoleListResponseDTO>> response = roleController.listAll(xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
            
            verify(rolePort).findAll(command);
        }
    }
    
    @Test
    @DisplayName("Deve retornar página vazia quando não há roles na busca paginada")
    void shouldReturnEmptyPageWhenNoRolesInSearch() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        PageResultView<RoleSearchView> emptyPageResultView = new PageResultView<>(List.of(), 0, 10, 0L, 0, true, true, false, false);
        var command = RoleSearchQueryDTO.builder()
            .pageable(pageable)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toSearchCommand(any(Pageable.class), any(UUID.class))).thenReturn(command);
            when(rolePort.findAll(any(RoleSearchQueryDTO.class))).thenReturn(emptyPageResultView);
            
            // When
            ResponseEntity<PageResultView<RoleSearchResponseDTO>> response = roleController.search(xApplication, pageable);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().getContent().isEmpty());
            assertEquals(0L, response.getBody().getTotalElements());
            
            verify(rolePort).findAll(command);
        }
    }
    
    @Test
    @DisplayName("Deve criar role com nome em maiúsculo")
    void shouldCreateRoleWithUppercaseName() {
        // Given
        RoleCreateDTO createDTO = RoleCreateDTO.builder()
            .name("user")
            .description("Usuário comum")
            .build();
        
        RoleResponseDTO responseDTO = RoleResponseDTO.builder()
            .id(roleId)
            .name("USER")
            .description("Usuário comum")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        var command = RoleCreateCommandDTO.builder()
            .name("user")
            .description("Usuário comum")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toCreateCommand(any(RoleCreateDTO.class), any(UUID.class))).thenReturn(command);
            when(rolePort.create(any(RoleCreateCommandDTO.class))).thenReturn(createRoleView);
            when(mapper.toCreateResponseDTO(any(RoleCreateView.class))).thenReturn(RoleCreateResponseDTO.builder()
                .id(roleId)
                .name("USER")
                .description("Usuário comum")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<RoleCreateResponseDTO> response = roleController.create(xApplication, createDTO);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("USER", response.getBody().getName());
            
            verify(rolePort).create(command);
        }
    }
    
    @Test
    @DisplayName("Deve atualizar role com nome em maiúsculo")
    void shouldUpdateRoleWithUppercaseName() {
        // Given
        RoleUpdateDTO updateDTO = RoleUpdateDTO.builder()
            .name("admin_updated")
            .description("Administrador atualizado")
            .build();
        
        RoleResponseDTO responseDTO = RoleResponseDTO.builder()
            .id(roleId)
            .name("ADMIN_UPDATED")
            .description("Administrador atualizado")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        var command = RoleUpdateCommandDTO.builder()
            .id(roleId)
            .name("admin_updated")
            .description("Administrador atualizado")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication)).thenReturn(xApplicationUuid);
            when(mapper.toUpdateCommand(any(UUID.class), any(RoleUpdateDTO.class), any(UUID.class))).thenReturn(command);
            when(rolePort.update(any(RoleUpdateCommandDTO.class))).thenReturn(updateRoleView);
            when(mapper.toUpdateResponseDTO(any(RoleUpdateView.class))).thenReturn(RoleUpdateResponseDTO.builder()
                .id(roleId)
                .name("ADMIN_UPDATED")
                .description("Administrador atualizado")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .active(true)
                .build());
            
            // When
            ResponseEntity<RoleUpdateResponseDTO> response = roleController.update(xApplication, roleId, updateDTO);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("ADMIN_UPDATED", response.getBody().getName());
            
            verify(rolePort).update(command);
        }
    }
}
