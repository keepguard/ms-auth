package com.keepguard.ms_auth.adapters.in.rest.user;

import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.*;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.response.*;
import com.keepguard.ms_auth.adapters.in.rest.user.mapper.UserAdapterMapper;
import com.keepguard.ms_auth.application.port.in.UserPort;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UserController
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    
    @Mock
    private UserPort userService;
    
    @Mock
    private UserAdapterMapper mapper;
    
    @InjectMocks
    private UserController userController;
    
    private UserCreateRequestDTO userCreateDTO;
    private UserResponseDTO userResponseDTO;
    private UserView userView;
    private UserGetByUsernameView userGetByUsernameView;
    private UserGetByEmailView userGetByEmailView;
    private UserGetByCodeView userGetByCodeView;
    private UserGetByIdExternalView userGetByIdExternalView;
    private UUID userId;
    private UUID xApplicationUuid;
    private String xApplication;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        xApplicationUuid = UUID.randomUUID();
        xApplication = xApplicationUuid.toString();
        
        userCreateDTO = UserTestBuilder.builder()
            .buildCreateRequestDTO();
        
        userResponseDTO = UserTestBuilder.builder()
            .withId(userId)
            .buildResponseDTO();
        
        userView = new UserView(
            userId,
            "testuser",
            "test@example.com",
            null,
            "ext-123",
            "code-123",
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(), // lastLogin
            null, // roles
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID() // xApplication
        );

        userGetByUsernameView = UserTestBuilder.builder().withId(userId).buildGetByUsernameView();
        userGetByEmailView = UserTestBuilder.builder().withId(userId).buildGetByEmailView();
        userGetByCodeView = UserTestBuilder.builder().withId(userId).buildGetByCodeView();
        userGetByIdExternalView = UserTestBuilder.builder().withId(userId).buildGetByIdExternalView();
    }
    
    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        // Given
        UserCreateCommandDTO command = UserTestBuilder.builder()
            .withXApplication(xApplicationUuid)
            .buildCreateCommand();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toCreateCommand(userCreateDTO, xApplicationUuid)).thenReturn(command);
            when(userService.create(command)).thenReturn(userView);
            when(mapper.toResponseDTO(userView)).thenReturn(userResponseDTO);
            
            // When
            ResponseEntity<UserResponseDTO> response = userController.create(userCreateDTO, xApplication);
            
            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            UserResponseDTO responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(userId, responseBody.getId());
            assertEquals("testuser", responseBody.getUsername());
            assertEquals("test@example.com", responseBody.getEmail());
            
            verify(mapper, times(1)).toCreateCommand(userCreateDTO, xApplicationUuid);
            verify(userService, times(1)).create(command);
            verify(mapper, times(1)).toResponseDTO(userView);
        }
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante criação de usuário")
    void shouldHandleExceptionsDuringUserCreation() {
        // Given
        UserCreateCommandDTO command = UserTestBuilder.builder()
            .withXApplication(xApplicationUuid)
            .buildCreateCommand();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toCreateCommand(userCreateDTO, xApplicationUuid)).thenReturn(command);
            when(userService.create(command))
                .thenThrow(new RuntimeException("Service error"));
            
            // When & Then
            assertThrows(RuntimeException.class, () -> {
                userController.create(userCreateDTO, xApplication);
            });
            
            verify(mapper, times(1)).toCreateCommand(userCreateDTO, xApplicationUuid);
            verify(userService, times(1)).create(command);
        }
    }
    
    @Test
    @DisplayName("Deve validar email do usuário com sucesso")
    void shouldValidateUserEmailSuccessfully() {
        // Given
        UserValidateEmailRequestDTO validateEmailDTO = UserTestBuilder.builder()
            .buildValidateEmailRequestDTO();
        
        UserValidateEmailCommandDTO command = UserTestBuilder.builder()
            .withXApplication(xApplicationUuid)
            .buildValidateEmailCommand();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toValidateEmailCommand(validateEmailDTO, xApplicationUuid)).thenReturn(command);
            doNothing().when(userService).validateEmailUser(command);
            
            // When
            ResponseEntity<Void> response = userController.validateEmail(validateEmailDTO, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(mapper, times(1)).toValidateEmailCommand(validateEmailDTO, xApplicationUuid);
            verify(userService, times(1)).validateEmailUser(command);
        }
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante validação de email")
    void shouldHandleExceptionsDuringEmailValidation() {
        // Given
        UserValidateEmailRequestDTO validateEmailDTO = UserTestBuilder.builder()
            .buildValidateEmailRequestDTO();
        
        UserValidateEmailCommandDTO command = UserTestBuilder.builder()
            .withXApplication(xApplicationUuid)
            .buildValidateEmailCommand();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toValidateEmailCommand(validateEmailDTO, xApplicationUuid)).thenReturn(command);
            doThrow(new RuntimeException("User not found"))
                .when(userService).validateEmailUser(command);
            
            // When & Then
            assertThrows(RuntimeException.class, () -> {
                userController.validateEmail(validateEmailDTO, xApplication);
            });
            
            verify(mapper, times(1)).toValidateEmailCommand(validateEmailDTO, xApplicationUuid);
            verify(userService, times(1)).validateEmailUser(command);
        }
    }
    
    @Test
    @DisplayName("Deve testar ValidateEmailRequestDTO com dados válidos")
    void shouldTestValidateEmailRequestDTOWithValidData() {
        // When
        UserValidateEmailRequestDTO dto = UserTestBuilder.builder()
            .withIdUserExternal(UUID.randomUUID())
            .buildValidateEmailRequestDTO();
        
        // Then
        assertNotNull(dto);
        assertNotNull(dto.getIdUserExternal());
    }
    
    @Test
    @DisplayName("Deve testar ValidateEmailRequestDTO com construtor padrão")
    void shouldTestValidateEmailRequestDTOWithDefaultConstructor() {
        // When
        UserValidateEmailRequestDTO dto = new UserValidateEmailRequestDTO();
        dto.setIdUserExternal("ext-456");
        
        // Then
        assertNotNull(dto);
        assertEquals("ext-456", dto.getIdUserExternal());
    }
    
    @Test
    @DisplayName("Deve testar ValidateEmailRequestDTO com todos os construtores")
    void shouldTestValidateEmailRequestDTOWithAllConstructors() {
        // Given
        String idUserExternal = "ext-789";
        
        // When - Testando construtor com parâmetros
        UserValidateEmailRequestDTO dto1 = new UserValidateEmailRequestDTO(idUserExternal);
        
        // Then
        assertNotNull(dto1);
        assertEquals(idUserExternal, dto1.getIdUserExternal());
        
        // When - Testando builder
        UserValidateEmailRequestDTO dto2 = UserValidateEmailRequestDTO.builder()
            .idUserExternal(idUserExternal)
            .build();
        
        // Then
        assertNotNull(dto2);
        assertEquals(idUserExternal, dto2.getIdUserExternal());
    }
    
    @Test
    @DisplayName("Deve testar UserControllerMapper.toCommand com dados válidos")
    void shouldTestUserControllerMapperToCommandWithValidData() {
        // Given
        UUID testUuid = UUID.randomUUID();
        UserCreateRequestDTO dto = UserTestBuilder.builder()
            .buildCreateRequestDTO();
        
        UserCreateCommandDTO expectedCommand = UserTestBuilder.builder()
            .withXApplication(testUuid)
            .buildCreateCommand();
        
        when(mapper.toCreateCommand(any(UserCreateRequestDTO.class), any(UUID.class))).thenReturn(expectedCommand);
        
        // When
        UserCreateCommandDTO result = mapper.toCreateCommand(dto, testUuid);
        
        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        assertNotNull(result.getIdUserExternal());
        verify(mapper, times(1)).toCreateCommand(dto, testUuid);
    }
    
    @Test
    @DisplayName("Deve testar UserControllerMapper.toSearchCriteria com dados válidos")
    void shouldTestUserControllerMapperToSearchCriteriaWithValidData() {
        // Given
        UUID testUuid = UUID.randomUUID();
        UUID idUserExternalUuid = UUID.randomUUID();
        UserSearchRequestDTO dto = UserTestBuilder.builder()
            .withIdUserExternal(idUserExternalUuid)
            .buildSearchRequestDTO();
        
        UserSearchQueryDTO expectedCommand = UserSearchQueryDTO.builder()
            .username("testuser")
            .email("test@example.com")
            .idUserExternal(dto.getIdUserExternal())
            .status(com.keepguard.ms_auth.domain.enums.UserStatus.ACTIVE)
            .page(0)
            .size(10)
            .sortBy("username")
            .sortDirection("ASC")
            .xApplicationUuid(testUuid)
            .build();
        
        // When
        when(mapper.toSearchQuery(any(UserSearchRequestDTO.class), any(UUID.class))).thenReturn(expectedCommand);
        var result = mapper.toSearchQuery(dto, testUuid);
        
        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(dto.getIdUserExternal(), result.getIdUserExternal());
        assertEquals(com.keepguard.ms_auth.domain.enums.UserStatus.ACTIVE, result.getStatus());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals("username", result.getSortBy());
        assertEquals("ASC", result.getSortDirection());
    }
    
    @Test
    @DisplayName("Deve testar UserControllerMapper.toResponseDTO com dados válidos")
    void shouldTestUserControllerMapperToResponseDTOWithValidData() {
        // Given
        UserView view = new UserView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            "ext-123",
            "code-123",
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(), // lastLogin
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID() // xApplication
        );
        
        when(mapper.toResponseDTO(view)).thenReturn(userResponseDTO);
        
        // When
        UserResponseDTO result = mapper.toResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(com.keepguard.ms_auth.domain.enums.UserStatus.ACTIVE, result.getStatus());
        assertTrue(result.isEmailVerified());
        
        verify(mapper, times(1)).toResponseDTO(view);
    }
    
    @Test
    @DisplayName("Deve testar UserControllerMapper.toDetailsResponseDTO com dados válidos")
    void shouldTestUserControllerMapperToDetailsResponseDTOWithValidData() {
        // Given
        UserView view = new UserView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            "ext-123",
            "code-123",
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(), // lastLogin
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID() // xApplication
        );
        
        UserDetailsResponseDTO expectedDetails = UserDetailsResponseDTO.builder()
            .id(userId)
            .idUserExternal(UUID.randomUUID())
            .codeUser(UUID.randomUUID())
            .username("testuser")
            .email("test@example.com")
            .status("ACTIVE")
            .emailVerified(true)
            .createdAt(LocalDateTime.now().toString())
            .updatedAt(LocalDateTime.now().toString())
            .roles(List.of("ROLE_USER"))
            .build();
        
        when(mapper.toDetailsResponseDTO(view)).thenReturn(expectedDetails);
        
        // When
        UserDetailsResponseDTO result = mapper.toDetailsResponseDTO(view);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("ACTIVE", result.getStatus());
        assertTrue(result.getEmailVerified());
        assertEquals(1, result.getRoles().size());
        
        verify(mapper, times(1)).toDetailsResponseDTO(view);
    }
    
    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        // Given
        String idUserExternal = "ext-123";
        UserStatusReasonRequestDTO statusReasonDTO = UserTestBuilder.builder()
            .buildStatusReasonRequestDTO();
        
        UserDeleteCommandDTO command = UserDeleteCommandDTO.builder()
            .idUserExternal(idUserExternal)
            .reason("Test reason")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toDeleteCommand(idUserExternal, "Test reason", xApplicationUuid)).thenReturn(command);
            doNothing().when(userService).delete(command);
            
            // When
            ResponseEntity<Void> response = userController.delete(idUserExternal, statusReasonDTO, xApplication);
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(mapper, times(1)).toDeleteCommand(idUserExternal, "Test reason", xApplicationUuid);
            verify(userService, times(1)).delete(command);
        }
    }
    
    @Test
    @DisplayName("Deve bloquear usuário com sucesso")
    void shouldBlockUserSuccessfully() {
        // Given
        String idUserExternal = "ext-123";
        UserStatusReasonRequestDTO statusReasonDTO = UserTestBuilder.builder()
            .buildStatusReasonRequestDTO();
        
        UserBlockCommandDTO command = UserBlockCommandDTO.builder()
            .idUserExternal(idUserExternal)
            .reason("Test reason")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toBlockCommand(idUserExternal, "Test reason", xApplicationUuid)).thenReturn(command);
            doNothing().when(userService).block(command);
            
            // When
            ResponseEntity<Void> response = userController.block(idUserExternal, statusReasonDTO, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(mapper, times(1)).toBlockCommand(idUserExternal, "Test reason", xApplicationUuid);
            verify(userService, times(1)).block(command);
        }
    }
    
    @Test
    @DisplayName("Deve desbloquear usuário com sucesso")
    void shouldUnlockUserSuccessfully() {
        // Given
        String idUserExternal = "ext-123";
        UserStatusReasonRequestDTO statusReasonDTO = UserTestBuilder.builder()
            .buildStatusReasonRequestDTO();
        
        UserUnlockCommandDTO command = UserUnlockCommandDTO.builder()
            .idUserExternal(idUserExternal)
            .reason("Test reason")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toUnlockCommand(idUserExternal, "Test reason", xApplicationUuid)).thenReturn(command);
            doNothing().when(userService).unlock(command);
            
            // When
            ResponseEntity<Void> response = userController.unlock(idUserExternal, statusReasonDTO, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(mapper, times(1)).toUnlockCommand(idUserExternal, "Test reason", xApplicationUuid);
            verify(userService, times(1)).unlock(command);
        }
    }
    
    @Test
    @DisplayName("Deve buscar usuário por código com sucesso")
    void shouldGetUserByCodeSuccessfully() {
        // Given
        String codeUser = "code-123";
        
        UserGetByCodeQueryDTO query = UserGetByCodeQueryDTO.builder()
            .codeUser(codeUser)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        UserByCodeResponseDTO userByCodeResponseDTO = UserByCodeResponseDTO.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .status("ACTIVE")
            .emailVerified(true)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toGetByCodeQuery(codeUser, xApplicationUuid)).thenReturn(query);
            when(userService.findByCodeUser(query)).thenReturn(userGetByCodeView);
            when(mapper.toUserByCodeResponseDTO(userGetByCodeView)).thenReturn(userByCodeResponseDTO);
            
            // When
            ResponseEntity<UserByCodeResponseDTO> response = userController.getByCodeUser(codeUser, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            UserByCodeResponseDTO responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(userId, responseBody.getId());
            assertEquals("testuser", responseBody.getUsername());
            assertEquals("test@example.com", responseBody.getEmail());
            
            verify(mapper, times(1)).toGetByCodeQuery(codeUser, xApplicationUuid);
            verify(userService, times(1)).findByCodeUser(query);
            verify(mapper, times(1)).toUserByCodeResponseDTO(userGetByCodeView);
        }
    }
    
    @Test
    @DisplayName("Deve buscar usuário por ID externo com sucesso")
    void shouldGetUserByIdExternalSuccessfully() {
        // Given
        String idUserExternal = "ext-123";
        
        UserGetByIdExternalQueryDTO query = UserGetByIdExternalQueryDTO.builder()
            .idUserExternal(idUserExternal)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        UserByIdExternalResponseDTO userByIdExternalResponseDTO = UserByIdExternalResponseDTO.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .status("ACTIVE")
            .emailVerified(true)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toGetByIdExternalQuery(idUserExternal, xApplicationUuid)).thenReturn(query);
            when(userService.findByIdUserExternal(query)).thenReturn(userGetByIdExternalView);
            when(mapper.toUserByIdExternalResponseDTO(userGetByIdExternalView)).thenReturn(userByIdExternalResponseDTO);
            
            // When
            ResponseEntity<UserByIdExternalResponseDTO> response = userController.getByIdUserExternal(idUserExternal, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            UserByIdExternalResponseDTO responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(userId, responseBody.getId());
            assertEquals("testuser", responseBody.getUsername());
            assertEquals("test@example.com", responseBody.getEmail());
            
            verify(mapper, times(1)).toGetByIdExternalQuery(idUserExternal, xApplicationUuid);
            verify(userService, times(1)).findByIdUserExternal(query);
            verify(mapper, times(1)).toUserByIdExternalResponseDTO(userGetByIdExternalView);
        }
    }
    
    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void shouldGetUserByEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        
        UserGetByEmailQueryDTO query = UserGetByEmailQueryDTO.builder()
            .email(email)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        UserByEmailResponseDTO userByEmailResponseDTO = UserByEmailResponseDTO.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .status("ACTIVE")
            .emailVerified(true)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toGetByEmailQuery(email, xApplicationUuid)).thenReturn(query);
            when(userService.findByEmail(query)).thenReturn(userGetByEmailView);
            when(mapper.toUserByEmailResponseDTO(userGetByEmailView)).thenReturn(userByEmailResponseDTO);
            
            // When
            ResponseEntity<UserByEmailResponseDTO> response = userController.getByEmail(email, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            UserByEmailResponseDTO responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(userId, responseBody.getId());
            assertEquals("testuser", responseBody.getUsername());
            assertEquals("test@example.com", responseBody.getEmail());
            
            verify(mapper, times(1)).toGetByEmailQuery(email, xApplicationUuid);
            verify(userService, times(1)).findByEmail(query);
            verify(mapper, times(1)).toUserByEmailResponseDTO(userGetByEmailView);
        }
    }
    
    @Test
    @DisplayName("Deve buscar usuário por username com sucesso")
    void shouldGetUserByUsernameSuccessfully() {
        // Given
        String username = "testuser";
        
        UserGetByUsernameQueryDTO query = UserGetByUsernameQueryDTO.builder()
            .username(username)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        UserByUsernameResponseDTO userByUsernameResponseDTO = UserByUsernameResponseDTO.builder()
            .id(userId)
            .username("testuser")
            .email("test@example.com")
            .status("ACTIVE")
            .emailVerified(true)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toGetByUsernameQuery(username, xApplicationUuid)).thenReturn(query);
            when(userService.findByUsername(query)).thenReturn(userGetByUsernameView);
            when(mapper.toUserByUsernameResponseDTO(userGetByUsernameView)).thenReturn(userByUsernameResponseDTO);
            
            // When
            ResponseEntity<UserByUsernameResponseDTO> response = userController.getByUsername(username, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            UserByUsernameResponseDTO responseBody = response.getBody();
            assertNotNull(responseBody);
            assertEquals(userId, responseBody.getId());
            assertEquals("testuser", responseBody.getUsername());
            assertEquals("test@example.com", responseBody.getEmail());
            
            verify(mapper, times(1)).toGetByUsernameQuery(username, xApplicationUuid);
            verify(userService, times(1)).findByUsername(query);
            verify(mapper, times(1)).toUserByUsernameResponseDTO(userGetByUsernameView);
        }
    }
    
    @Test
    @DisplayName("Deve adicionar role ao usuário com sucesso")
    void shouldAddRoleToUserSuccessfully() {
        // Given
        String idUserExternal = "ext-123";
        UserAddRoleToUserRequestDTO addRoleDTO = UserTestBuilder.builder()
            .buildAddRoleToUserRequestDTO();
        
        UserAddRoleCommandDTO command = UserAddRoleCommandDTO.builder()
            .idUserExternal(idUserExternal)
            .role("ROLE_ADMIN")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toAddRoleCommand(idUserExternal, "ROLE_ADMIN", xApplicationUuid)).thenReturn(command);
            doNothing().when(userService).addRoleToUser(command);
            
            // When
            ResponseEntity<Void> response = userController.addRoleToUser(idUserExternal, addRoleDTO, xApplication);
            
            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            verify(mapper, times(1)).toAddRoleCommand(idUserExternal, "ROLE_ADMIN", xApplicationUuid);
            verify(userService, times(1)).addRoleToUser(command);
        }
    }
    
    @Test
    @DisplayName("Deve remover role do usuário com sucesso")
    void shouldRemoveRoleFromUserSuccessfully() {
        // Given
        String idUserExternal = "ext-123";
        String role = "ROLE_ADMIN";
        
        UserRemoveRoleCommandDTO command = UserRemoveRoleCommandDTO.builder()
            .idUserExternal(idUserExternal)
            .role(role)
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toRemoveRoleCommand(idUserExternal, role, xApplicationUuid)).thenReturn(command);
            doNothing().when(userService).removeRoleFromUser(command);
            
            // When
            ResponseEntity<Void> response = userController.removeRoleFromUser(idUserExternal, role, xApplication);
            
            // Then
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(mapper, times(1)).toRemoveRoleCommand(idUserExternal, role, xApplicationUuid);
            verify(userService, times(1)).removeRoleFromUser(command);
        }
    }
    
    @Test
    @DisplayName("Deve atualizar email do usuário com sucesso")
    void shouldUpdateUserEmailSuccessfully() {
        // Given
        String idUserExternal = "ext-123";
        UserUpdateEmailRequestDTO updateEmailDTO = UserTestBuilder.builder()
            .buildUpdateEmailRequestDTO();
        
        UserUpdateEmailCommandDTO command = UserUpdateEmailCommandDTO.builder()
            .idUserExternal(idUserExternal)
            .newEmail("newemail@example.com")
            .xApplicationUuid(xApplicationUuid)
            .build();
        
        try (MockedStatic<ValidationUtils> mockedValidation = mockStatic(ValidationUtils.class)) {
            mockedValidation.when(() -> ValidationUtils.validateXApplication(xApplication))
                .thenReturn(xApplicationUuid);
            
            when(mapper.toUpdateEmailCommand(idUserExternal, "newemail@example.com", xApplicationUuid)).thenReturn(command);
            doNothing().when(userService).updateUserEmail(command);
            
            // When
            ResponseEntity<Void> response = userController.updateUserEmail(idUserExternal, updateEmailDTO, xApplication);
            
            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(mapper, times(1)).toUpdateEmailCommand(idUserExternal, "newemail@example.com", xApplicationUuid);
            verify(userService, times(1)).updateUserEmail(command);
        }
    }
}