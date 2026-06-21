package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.port.out.cache.UserCachePort;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.application.mapper.UserApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserStatusHistoryRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.user.UserRole;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Command Service Tests")
class UserCommandServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private RoleRepositoryPort roleRepository;
    @Mock private UserRoleRepositoryPort userRoleRepository;
    @Mock private UserStatusHistoryRepositoryPort userStatusHistoryRepository;
    @Mock private UserApplicationMapper userApplicationMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MetricsPort metricsPort;
    @Mock private UserCachePort userCachePort;

    @InjectMocks private UserCommandService userCommandService;

    private User user;
    private UserCreateCommandDTO userCreateCommand;
    private Role role;
    private UUID userId;
    private UUID idUserExternal;
    private UUID xApplicationUuid;
    private UserTestBuilder userTestBuilder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        idUserExternal = UUID.randomUUID();
        xApplicationUuid = UUID.randomUUID();
        
        userTestBuilder = UserTestBuilder.builder()
            .withId(userId)
            .withIdUserExternal(idUserExternal)
            .withXApplication(xApplicationUuid);
            
        user = userTestBuilder.buildDomain();
        userCreateCommand = userTestBuilder.buildCreateCommand();
            
        role = RoleTestBuilder.builder()
            .withName("ADMIN")
            .buildDomain();
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        // Given
        when(userRepository.findByUsernameAndXApplication("testuser", xApplicationUuid)).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndXApplication("test@example.com", xApplicationUuid)).thenReturn(Optional.empty());
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());
        when(userStatusHistoryRepository.save(any(UserStatusHistory.class))).thenReturn(new UserStatusHistory());
        when(userApplicationMapper.toView(user)).thenReturn(UserTestBuilder.builder().withId(userId).buildView());

        // When
        var result = userCommandService.create(userCreateCommand);

        // Then
        assertNotNull(result);
        verify(userRepository).findByUsernameAndXApplication("testuser", xApplicationUuid);
        verify(userRepository).findByEmailAndXApplication("test@example.com", xApplicationUuid);
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(userRepository).save(any(User.class));
        verify(roleRepository).findByName("ADMIN");
        verify(userRoleRepository).save(any(UserRole.class));
        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
        verify(metricsPort).incrementCounter(eq("user_created_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando username já existe")
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        when(userRepository.findByUsernameAndXApplication("testuser", xApplicationUuid)).thenReturn(Optional.of(user));

        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, 
            () -> userCommandService.create(userCreateCommand));
        
        assertEquals("Username já existe: testuser", exception.getMessage());
        verify(metricsPort).incrementCounter(eq("user_business_errors_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando email já existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.findByUsernameAndXApplication("testuser", xApplicationUuid)).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndXApplication("test@example.com", xApplicationUuid)).thenReturn(Optional.of(user));

        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, 
            () -> userCommandService.create(userCreateCommand));
        
        assertEquals("Email já existe: test@example.com", exception.getMessage());
        verify(metricsPort).incrementCounter(eq("user_business_errors_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando ID externo já existe")
    void shouldThrowExceptionWhenIdExternalAlreadyExists() {
        // Given
        when(userRepository.findByUsernameAndXApplication("testuser", xApplicationUuid)).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndXApplication("test@example.com", xApplicationUuid)).thenReturn(Optional.empty());
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));

        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, 
            () -> userCommandService.create(userCreateCommand));
        
        assertEquals("ID externo já existe: " + idUserExternal, exception.getMessage());
        verify(metricsPort).incrementCounter(eq("user_business_errors_total"), any());
    }

    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userStatusHistoryRepository.save(any(UserStatusHistory.class))).thenReturn(new UserStatusHistory());
        doNothing().when(userCachePort).removeUserFromCache(any(User.class));

        // When
        var deleteCommand = userTestBuilder.buildDeleteCommand();
        userCommandService.delete(deleteCommand);

        // Then
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(userRepository).save(any(User.class));
        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
        verify(userCachePort).removeUserFromCache(any(User.class));
        verify(metricsPort).incrementCounter(eq("user_deleted_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado para deletar")
    void shouldThrowExceptionWhenUserNotFoundForDelete() {
        // Given
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> {
                var deleteCommand = userTestBuilder.buildDeleteCommand();
                userCommandService.delete(deleteCommand);
            });
        
        assertEquals("Usuário não encontrado: " + idUserExternal, exception.getMessage());
    }

    @Test
    @DisplayName("Deve bloquear usuário com sucesso")
    void shouldBlockUserSuccessfully() {
        // Given
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userStatusHistoryRepository.save(any(UserStatusHistory.class))).thenReturn(new UserStatusHistory());
        doNothing().when(userCachePort).removeUserFromCache(any(User.class));

        // When
        var blockCommand = userTestBuilder.buildBlockCommand();
        userCommandService.block(blockCommand);

        // Then
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(userRepository).save(any(User.class));
        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
        verify(userCachePort).removeUserFromCache(any(User.class));
        verify(metricsPort).incrementCounter(eq("user_blocked_total"), any());
    }

    @Test
    @DisplayName("Deve desbloquear usuário com sucesso")
    void shouldUnlockUserSuccessfully() {
        // Given
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userStatusHistoryRepository.save(any(UserStatusHistory.class))).thenReturn(new UserStatusHistory());
        doNothing().when(userCachePort).removeUserFromCache(any(User.class));

        // When
        var unlockCommand = userTestBuilder.buildUnlockCommand();
        userCommandService.unlock(unlockCommand);

        // Then
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(userRepository).save(any(User.class));
        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
        verify(userCachePort).removeUserFromCache(any(User.class));
        verify(metricsPort).incrementCounter(eq("user_unlocked_total"), any());
    }

    @Test
    @DisplayName("Deve validar email do usuário com sucesso")
    void shouldValidateEmailUserSuccessfully() {
        // Given
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userStatusHistoryRepository.save(any(UserStatusHistory.class))).thenReturn(new UserStatusHistory());
        doNothing().when(userCachePort).removeUserFromCache(any(User.class));

        // When
        var validateCommand = userTestBuilder.buildValidateEmailCommand();
        userCommandService.validateEmailUser(validateCommand);

        // Then
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(userRepository).save(any(User.class));
        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
        verify(userCachePort).removeUserFromCache(any(User.class));
        verify(metricsPort).incrementCounter(eq("user_email_validated_total"), any());
    }

    @Test
    @DisplayName("Deve adicionar role ao usuário com sucesso")
    void shouldAddRoleToUserSuccessfully() {
        // Given
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByUserIdAndRoleId(userId, role.getId())).thenReturn(Optional.empty());
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());
        doNothing().when(userCachePort).removeUserFromCache(any(User.class));

        // When
        var addRoleCommand = userTestBuilder.buildAddRoleCommand();
        userCommandService.addRoleToUser(addRoleCommand);

        // Then
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(roleRepository).findByName("ADMIN");
        verify(userRoleRepository).findByUserIdAndRoleId(userId, role.getId());
        verify(userRoleRepository).save(any(UserRole.class));
        verify(userCachePort).removeUserFromCache(any(User.class));
        verify(metricsPort).incrementCounter(eq("user_role_added_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando role já está atribuída ao usuário")
    void shouldThrowExceptionWhenRoleAlreadyAssigned() {
        // Given
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByUserIdAndRoleId(userId, role.getId())).thenReturn(Optional.of(new UserRole()));

        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, 
            () -> {
                var addRoleCommand = userTestBuilder.buildAddRoleCommand();
                userCommandService.addRoleToUser(addRoleCommand);
            });
        
        assertEquals("Usuário já possui a role: ADMIN", exception.getMessage());
        verify(metricsPort).incrementCounter(eq("user_business_errors_total"), any());
    }

    @Test
    @DisplayName("Deve remover role do usuário com sucesso")
    void shouldRemoveRoleFromUserSuccessfully() {
        // Given
        UserRole userRole = new UserRole();
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(role));
        when(userRoleRepository.findByUserIdAndRoleId(userId, role.getId())).thenReturn(Optional.of(userRole));
        doNothing().when(userCachePort).removeUserFromCache(any(User.class));

        // When
        var removeRoleCommand = userTestBuilder.buildRemoveRoleCommand();
        userCommandService.removeRoleFromUser(removeRoleCommand);

        // Then
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(roleRepository).findByName("ADMIN");
        verify(userRoleRepository).findByUserIdAndRoleId(userId, role.getId());
        verify(userRoleRepository).delete(userRole);
        verify(userCachePort).removeUserFromCache(any(User.class));
        verify(metricsPort).incrementCounter(eq("user_role_removed_total"), any());
    }


    @Test
    @DisplayName("Deve atualizar email do usuário com sucesso")
    void shouldUpdateUserEmailSuccessfully() {
        // Given
        String newEmail = "newemail@example.com";
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(userRepository.findByEmailAndXApplication(newEmail, xApplicationUuid)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userStatusHistoryRepository.save(any(UserStatusHistory.class))).thenReturn(new UserStatusHistory());
        doNothing().when(userCachePort).removeUserFromCache(any(User.class));

        // When
        var updateEmailCommand = userTestBuilder.buildUpdateEmailCommand(idUserExternal, newEmail);
        userCommandService.updateUserEmail(updateEmailCommand);

        // Then
        verify(userRepository).findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid);
        verify(userRepository).findByEmailAndXApplication(newEmail, xApplicationUuid);
        verify(userRepository).save(any(User.class));
        verify(userStatusHistoryRepository).save(any(UserStatusHistory.class));
        verify(userCachePort).removeUserFromCache(any(User.class));
        verify(metricsPort).incrementCounter(eq("user_email_updated_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando novo email já existe")
    void shouldThrowExceptionWhenNewEmailAlreadyExists() {
        // Given
        String newEmail = "newemail@example.com";
        when(userRepository.findByIdUserExternalAndXApplication(idUserExternal, xApplicationUuid)).thenReturn(Optional.of(user));
        when(userRepository.findByEmailAndXApplication(newEmail, xApplicationUuid)).thenReturn(Optional.of(user));

        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, 
            () -> {
                var updateEmailCommand = userTestBuilder.buildUpdateEmailCommand(idUserExternal, newEmail);
                userCommandService.updateUserEmail(updateEmailCommand);
            });
        
        assertEquals("Email já existe: " + newEmail, exception.getMessage());
        verify(metricsPort).incrementCounter(eq("user_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve testar entidade User com dados válidos")
    void shouldTestUserEntityWithValidData() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        String username = "testuser";
        String email = "test@example.com";
        String passwordHash = "hashedpassword";
        UserStatus status = UserStatus.ACTIVE;
        Boolean emailVerified = true;
        LocalDateTime now = LocalDateTime.now();
        
        // When
        User user = User.builder()
            .id(userId)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username(username)
            .email(email)
            .passwordHash(passwordHash)
            .status(status)
            .emailVerified(emailVerified)
            .createdAt(now)
            .updatedAt(now)
            .lastLogin(now)
            .build();
        
        // Then
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertEquals(idUserExternal, user.getIdUserExternal());
        assertEquals(codeUser, user.getCodeUser());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(passwordHash, user.getPasswordHash());
        assertEquals(status, user.getStatus());
        assertEquals(emailVerified, user.getEmailVerified());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
        assertEquals(now, user.getLastLogin());
    }
    
    @Test
    @DisplayName("Deve testar entidade User com construtor padrão")
    void shouldTestUserEntityWithDefaultConstructor() {
        // When
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setIdUserExternal(UUID.randomUUID());
        user.setCodeUser(UUID.randomUUID());
        user.setUsername("defaultuser");
        user.setEmail("default@example.com");
        user.setPasswordHash("defaultpassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        
        // Then
        assertNotNull(user);
        assertNotNull(user.getId());
        assertNotNull(user.getIdUserExternal());
        assertNotNull(user.getCodeUser());
        assertEquals("defaultuser", user.getUsername());
        assertEquals("default@example.com", user.getEmail());
        assertEquals("defaultpassword", user.getPasswordHash());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.getEmailVerified());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(user.getLastLogin());
    }
    
    @Test
    @DisplayName("Deve testar entidade User com todos os construtores")
    void shouldTestUserEntityWithAllConstructors() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        String username = "allconstructorsuser";
        String email = "allconstructors@example.com";
        String passwordHash = "allconstructorspassword";
        UserStatus status = UserStatus.ACTIVE;
        Boolean emailVerified = true;
        LocalDateTime now = LocalDateTime.now();
        
        // When - Testando builder com parâmetros
        User user1 = User.builder()
            .id(userId)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username(username)
            .email(email)
            .passwordHash(passwordHash)
            .status(status)
            .emailVerified(emailVerified)
            .createdAt(now)
            .updatedAt(now)
            .lastLogin(now)
            .companyId(UUID.randomUUID())
            .companyCode(UUID.randomUUID())
            .xApplication(UUID.randomUUID())
            .build();
        
        // Then
        assertNotNull(user1);
        assertEquals(userId, user1.getId());
        assertEquals(idUserExternal, user1.getIdUserExternal());
        assertEquals(codeUser, user1.getCodeUser());
        assertEquals(username, user1.getUsername());
        assertEquals(email, user1.getEmail());
        assertEquals(passwordHash, user1.getPasswordHash());
        assertEquals(status, user1.getStatus());
        assertEquals(emailVerified, user1.getEmailVerified());
        assertEquals(now, user1.getCreatedAt());
        assertEquals(now, user1.getUpdatedAt());
        assertEquals(now, user1.getLastLogin());
        
        // When - Testando builder
        User user2 = User.builder()
            .id(userId)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username(username)
            .email(email)
            .passwordHash(passwordHash)
            .status(status)
            .emailVerified(emailVerified)
            .createdAt(now)
            .updatedAt(now)
            .lastLogin(now)
            .build();
        
        // Then
        assertNotNull(user2);
        assertEquals(userId, user2.getId());
        assertEquals(idUserExternal, user2.getIdUserExternal());
        assertEquals(codeUser, user2.getCodeUser());
        assertEquals(username, user2.getUsername());
        assertEquals(email, user2.getEmail());
        assertEquals(passwordHash, user2.getPasswordHash());
        assertEquals(status, user2.getStatus());
        assertEquals(emailVerified, user2.getEmailVerified());
        assertEquals(now, user2.getCreatedAt());
        assertEquals(now, user2.getUpdatedAt());
        assertEquals(now, user2.getLastLogin());
    }
    
    @Test
    @DisplayName("Deve testar método preUpdate da entidade User")
    void shouldTestUserEntityPreUpdateMethod() {
        // Given
        User user = User.builder()
            .id(UUID.randomUUID())
            .idUserExternal(UUID.randomUUID())
            .codeUser(UUID.randomUUID())
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedpassword")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(LocalDateTime.now().minusHours(1))
            .updatedAt(LocalDateTime.now().minusHours(1))
            .build();
        
        LocalDateTime beforeUpdate = user.getUpdatedAt();
        
        // When
        user.updateTimestamp();
        
        // Then
        assertNotNull(user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(beforeUpdate));
    }
    
    @Test
    @DisplayName("Deve testar valores padrão da entidade User")
    void shouldTestUserEntityDefaultValues() {
        // When
        User user = User.builder()
            .id(UUID.randomUUID())
            .idUserExternal(UUID.randomUUID())
            .codeUser(UUID.randomUUID())
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedpassword")
            .build();
        
        // Then
        assertNotNull(user);
        assertEquals(UserStatus.ACTIVE, user.getStatus()); // Valor padrão
        assertFalse(user.getEmailVerified()); // Valor padrão
    }

}
