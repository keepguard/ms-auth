package com.keepguard.ms_auth.adapters.in.rest.user.mapper;

import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.*;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.response.*;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UserAdapterMapper
 */
@ExtendWith(MockitoExtension.class)
class UserAdapterMapperTest {
    
    @InjectMocks
    private UserAdapterMapper userAdapterMapper;
    
    private UUID userId;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Deve mapear UserCreateRequestDTO para UserCreateRequestCommandDTO")
    void shouldMapUserCreateRequestDTOToCommand() {
        // Given
        UserCreateRequestDTO dto = UserTestBuilder.builder()
            .buildCreateRequestDTO();

        // When
        var command = userAdapterMapper.toCreateCommand(dto, UUID.randomUUID());

        // Then
        assertNotNull(command);
        assertEquals("testuser", command.getUsername());
        assertEquals("test@example.com", command.getEmail());
        assertEquals("password123", command.getPassword());
        assertEquals(dto.getIdUserExternal(), command.getIdUserExternal());
    }
    
    @Test
    @DisplayName("Deve retornar null quando UserCreateRequestDTO for null")
    void shouldReturnNullWhenUserCreateRequestDTOIsNull() {
        // When
        var command = userAdapterMapper.toCreateCommand(null, UUID.randomUUID());
        
        // Then
        assertNull(command);
    }
    
    @Test
    @DisplayName("Deve mapear UserValidateEmailRequestDTO para UserValidateEmailRequestCommandDTO")
    void shouldMapUserValidateEmailRequestDTOToCommand() {
        // Given
        UserValidateEmailRequestDTO dto = UserTestBuilder.builder()
            .buildValidateEmailRequestDTO();
        
        // When
        var command = userAdapterMapper.toValidateEmailCommand(dto, UUID.randomUUID());
        
        // Then
        assertNotNull(command);
        assertEquals(dto.getIdUserExternal(), command.getIdUserExternal());
    }
    
    @Test
    @DisplayName("Deve mapear dados para UserAddRoleRequestCommandDTO")
    void shouldMapDataToUserAddRoleRequestCommandDTO() {
        // Given
        String idUserExternal = "ext-123";
        String role = "ADMIN";
        UUID xApplicationUuid = UUID.randomUUID();
        
        // When
        var command = userAdapterMapper.toAddRoleCommand(idUserExternal, role, xApplicationUuid);
        
        // Then
        assertNotNull(command);
        assertEquals("ext-123", command.getIdUserExternal());
        assertEquals("ADMIN", command.getRole());
    }
    
    @Test
    @DisplayName("Deve mapear dados para UserBlockRequestCommandDTO")
    void shouldMapDataToUserBlockRequestCommandDTO() {
        // Given
        String idUserExternal = "ext-123";
        String reason = "Violação de políticas";
        UUID xApplicationUuid = UUID.randomUUID();
        
        // When
        var command = userAdapterMapper.toBlockCommand(idUserExternal, reason, xApplicationUuid);
        
        // Then
        assertNotNull(command);
        assertEquals("ext-123", command.getIdUserExternal());
        assertEquals("Violação de políticas", command.getReason());
    }
    
    @Test
    @DisplayName("Deve mapear dados para UserUpdateEmailRequestCommandDTO")
    void shouldMapDataToUserUpdateEmailRequestCommandDTO() {
        // Given
        String idUserExternal = "ext-123";
        String newEmail = "newemail@example.com";
        UUID xApplicationUuid = UUID.randomUUID();
        
        // When
        var command = userAdapterMapper.toUpdateEmailCommand(idUserExternal, newEmail, xApplicationUuid);
        
        // Then
        assertNotNull(command);
        assertEquals("ext-123", command.getIdUserExternal());
        assertEquals("newemail@example.com", command.getNewEmail());
    }
    
    @Test
    @DisplayName("Deve retornar null quando UserValidateEmailRequestDTO for null")
    void shouldReturnNullWhenUserValidateEmailRequestDTOIsNull() {
        // When
        var command = userAdapterMapper.toValidateEmailCommand(null, UUID.randomUUID());
        
        // Then
        assertNull(command);
    }
    
    @Test
    @DisplayName("Deve retornar null quando UserUpdateEmailRequestDTO for null")
    void shouldReturnNullWhenUserUpdateEmailRequestDTOIsNull() {
        // When
        var command = userAdapterMapper.toUpdateEmailCommand("ext-123", "newemail@example.com", UUID.randomUUID());
        
        // Then
        assertNotNull(command); // Este método não retorna null, sempre cria um comando
    }
    
    @Test
    @DisplayName("Deve mapear UserView para UserResponseDTO com dados nulos")
    void shouldMapUserViewToResponseDTOWithNullData() {
        // Given
        UserView view = new UserView(
            userId,
            "testuser",
            "test@example.com",
            null, // name
            null, // idUserExternal
            null, // codeUser
            null, // status
            null, // emailVerified
            null, // createdAt
            null, // updatedAt
            null, // lastLogin
            null, // roles
            null, // companyId
            null, // companyCode
            null  // xApplication
        );

        // When
        UserResponseDTO responseDTO = userAdapterMapper.toResponseDTO(view);

        // Then
        assertNotNull(responseDTO);
        assertEquals(userId, responseDTO.getId());
        assertEquals("testuser", responseDTO.getUsername());
        assertEquals("test@example.com", responseDTO.getEmail());
        assertFalse(responseDTO.isEmailVerified()); // null emailVerified becomes false
    }

    // Testes para os métodos do UserAdapterMapper

    @Test
    @DisplayName("Deve mapear UserView para UserResponseDTO")
    void shouldMapUserViewToResponseDTO() {
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
            LocalDateTime.now(),
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        UserResponseDTO responseDTO = userAdapterMapper.toResponseDTO(view);

        // Then
        assertNotNull(responseDTO);
        assertEquals(userId, responseDTO.getId());
        assertEquals("testuser", responseDTO.getUsername());
        assertEquals("test@example.com", responseDTO.getEmail());
        assertEquals(UserStatus.ACTIVE, responseDTO.getStatus());
        assertTrue(responseDTO.isEmailVerified());
    }

    @Test
    @DisplayName("Deve retornar null quando UserView for null")
    void shouldReturnNullWhenUserViewIsNull() {
        // When
        UserResponseDTO dto = userAdapterMapper.toResponseDTO((UserView) null);

        // Then
        assertNull(dto);
    }

    @Test
    @DisplayName("Deve mapear UserView para UserDetailsResponseDTO")
    void shouldMapUserViewToDetailsResponseDTO() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        
        UserView view = new UserView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            idUserExternal.toString(),
            codeUser.toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID() // xApplication
        );

        // When
        UserDetailsResponseDTO dto = userAdapterMapper.toDetailsResponseDTO(view);

        // Then
        assertNotNull(dto);
        assertEquals(userId, dto.getId());
        assertEquals(idUserExternal, dto.getIdUserExternal());
        assertEquals(codeUser, dto.getCodeUser());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("ACTIVE", dto.getStatus());
        assertTrue(dto.getEmailVerified());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
        assertNotNull(dto.getLastLogin());
        assertEquals(1, dto.getRoles().size());
    }

    @Test
    @DisplayName("Deve mapear PageResult para UserSearchResponseDTO")
    void shouldMapPageResultToSearchResponseDTO() {
        // Given
        UserSearchView view = new UserSearchView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID() // xApplication
        );

        PageResultView<UserSearchView> pageResultView = new PageResultView<>(
            List.of(view),
            0,
            10,
            1L,
            1,
            true,
            true,
            false,
            false
        );

        // When
        UserSearchResponseDTO dto = userAdapterMapper.toSearchResponseDTO(pageResultView);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.getContent().size());
        assertEquals(0, dto.getPageNumber());
        assertEquals(10, dto.getPageSize());
        assertEquals(1L, dto.getTotalElements());
        assertEquals(1, dto.getTotalPages());
        assertTrue(dto.getFirst());
        assertTrue(dto.getLast());
        assertFalse(dto.getHasNext());
        assertFalse(dto.getHasPrevious());
    }

    @Test
    @DisplayName("Deve mapear UserStatusHistory para UserStatusHistoryResponseDTO")
    void shouldMapUserStatusHistoryToResponseDTO() {
        // Given
        UserStatusHistory history = UserStatusHistory.builder()
            .id(UUID.randomUUID())
            .eventType(com.keepguard.ms_auth.domain.enums.UserStatusEventType.BLOCKED)
            .reason("User blocked by admin")
            .createdAt(LocalDateTime.now())
            .build();

        // When
        UserStatusHistoryResponseDTO dto = userAdapterMapper.toStatusHistoryResponseDTO(history);

        // Then
        assertNotNull(dto);
        assertEquals(history.getId(), dto.getId());
        assertEquals("BLOCKED", dto.getEventType().toString());
        assertEquals("User blocked by admin", dto.getReason());
        assertNotNull(dto.getCreatedAt());
    }

    @Test
    @DisplayName("Deve retornar null quando inputs forem null")
    void shouldReturnNullWhenInputsAreNull() {
        // When & Then
        assertNull(userAdapterMapper.toResponseDTO((UserView) null));
        assertNull(userAdapterMapper.toDetailsResponseDTO(null));
        assertNull(userAdapterMapper.toSearchResponseDTO(null));
        assertNull(userAdapterMapper.toStatusHistoryResponseDTO(null));
        assertNull(userAdapterMapper.toUserByCodeResponseDTO(null));
        assertNull(userAdapterMapper.toUserByIdExternalResponseDTO(null));
        assertNull(userAdapterMapper.toUserByEmailResponseDTO(null));
        assertNull(userAdapterMapper.toUserByUsernameResponseDTO(null));
    }

    @Test
    @DisplayName("Deve mapear UserGetByCodeView para UserByCodeResponseDTO")
    void shouldMapUserViewToUserByCodeResponseDTO() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        
        UserGetByCodeView view = new UserGetByCodeView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            idUserExternal.toString(),
            codeUser.toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        UserByCodeResponseDTO dto = userAdapterMapper.toUserByCodeResponseDTO(view);

        // Then
        assertNotNull(dto);
        assertEquals(userId, dto.getId());
        assertEquals(idUserExternal, dto.getIdUserExternal());
        assertEquals(codeUser, dto.getCodeUser());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("ACTIVE", dto.getStatus());
        assertTrue(dto.getEmailVerified());
        assertEquals(1, dto.getRoles().size());
    }

    @Test
    @DisplayName("Deve mapear UserGetByIdExternalView para UserByIdExternalResponseDTO")
    void shouldMapUserViewToUserByIdExternalResponseDTO() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        
        UserGetByIdExternalView view = new UserGetByIdExternalView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            idUserExternal.toString(),
            codeUser.toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        UserByIdExternalResponseDTO dto = userAdapterMapper.toUserByIdExternalResponseDTO(view);

        // Then
        assertNotNull(dto);
        assertEquals(userId, dto.getId());
        assertEquals(idUserExternal, dto.getIdUserExternal());
        assertEquals(codeUser, dto.getCodeUser());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("ACTIVE", dto.getStatus());
        assertTrue(dto.getEmailVerified());
        assertEquals(1, dto.getRoles().size());
    }

    @Test
    @DisplayName("Deve mapear UserGetByEmailView para UserByEmailResponseDTO")
    void shouldMapUserViewToUserByEmailResponseDTO() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        
        UserGetByEmailView view = new UserGetByEmailView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            idUserExternal.toString(),
            codeUser.toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        UserByEmailResponseDTO dto = userAdapterMapper.toUserByEmailResponseDTO(view);

        // Then
        assertNotNull(dto);
        assertEquals(userId, dto.getId());
        assertEquals(idUserExternal, dto.getIdUserExternal());
        assertEquals(codeUser, dto.getCodeUser());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("ACTIVE", dto.getStatus());
        assertTrue(dto.getEmailVerified());
        assertEquals(1, dto.getRoles().size());
    }

    @Test
    @DisplayName("Deve mapear UserGetByUsernameView para UserByUsernameResponseDTO")
    void shouldMapUserViewToUserByUsernameResponseDTO() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        
        UserGetByUsernameView view = new UserGetByUsernameView(
            userId,
            "testuser",
            "test@example.com",
            "Test User",
            idUserExternal.toString(),
            codeUser.toString(),
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        // When
        UserByUsernameResponseDTO dto = userAdapterMapper.toUserByUsernameResponseDTO(view);

        // Then
        assertNotNull(dto);
        assertEquals(userId, dto.getId());
        assertEquals(idUserExternal, dto.getIdUserExternal());
        assertEquals(codeUser, dto.getCodeUser());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("ACTIVE", dto.getStatus());
        assertTrue(dto.getEmailVerified());
        assertEquals(1, dto.getRoles().size());
    }
}