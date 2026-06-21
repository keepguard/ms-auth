package com.keepguard.ms_auth.domain.entity.user;

import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade User
 */
class UserTest {
    
    private User user;
    private UUID userId;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .withId(userId)
            .asActive()
            .buildDomain();
    }
    
    @Test
    @DisplayName("Deve criar usuário com dados válidos")
    void shouldCreateUserWithValidData() {
        // Then
        assertNotNull(user.getId());
        assertNotNull(user.getIdUserExternal());
        assertNotNull(user.getCodeUser());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@example.com", user.getEmail());
        assertNotNull(user.getPasswordHash());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(user.getEmailVerified());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertNull(user.getLastLogin());
    }
    
    @Test
    @DisplayName("Deve criar usuário com ID específico")
    void shouldCreateUserWithSpecificId() {
        // Given
        UUID specificId = UUID.randomUUID();
        
        // When
        User userWithId = UserTestBuilder.aUser()
            .withId(specificId)
            .buildDomain();
        
        // Then
        assertEquals(specificId, userWithId.getId());
    }
    
    @Test
    @DisplayName("Deve criar usuário ativo")
    void shouldCreateActiveUser() {
        // When
        User activeUser = UserTestBuilder.aUser()
            .asActive()
            .buildDomain();
        
        // Then
        assertEquals(UserStatus.ACTIVE, activeUser.getStatus());
        assertTrue(activeUser.getEmailVerified());
    }
    
    @Test
    @DisplayName("Deve criar usuário bloqueado")
    void shouldCreateBlockedUser() {
        // When
        User blockedUser = UserTestBuilder.aUser()
            .asBlocked()
            .buildDomain();
        
        // Then
        assertEquals(UserStatus.BLOCKED, blockedUser.getStatus());
    }
    
    @Test
    @DisplayName("Deve criar usuário deletado")
    void shouldCreateDeletedUser() {
        // When
        User deletedUser = UserTestBuilder.aUser()
            .asDeleted()
            .buildDomain();
        
        // Then
        assertEquals(UserStatus.DELETED, deletedUser.getStatus());
    }
    
    @Test
    @DisplayName("Deve criar usuário com email não verificado")
    void shouldCreateUserWithUnverifiedEmail() {
        // When
        User unverifiedUser = UserTestBuilder.aUser()
            .withUnverifiedEmail()
            .buildDomain();
        
        // Then
        assertFalse(unverifiedUser.getEmailVerified());
    }
    
    @Test
    @DisplayName("Deve criar usuário com último login")
    void shouldCreateUserWithLastLogin() {
        // Given
        LocalDateTime lastLogin = LocalDateTime.now().minusHours(1);
        
        // When
        User userWithLastLogin = UserTestBuilder.aUser()
            .withLastLogin(lastLogin)
            .buildDomain();
        
        // Then
        assertEquals(lastLogin, userWithLastLogin.getLastLogin());
    }
    
    @Test
    @DisplayName("Deve criar usuário com datas específicas")
    void shouldCreateUserWithSpecificDates() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        
        // When
        User userWithDates = UserTestBuilder.aUser()
            .withCreatedAt(createdAt)
            .withUpdatedAt(updatedAt)
            .buildDomain();
        
        // Then
        assertEquals(createdAt, userWithDates.getCreatedAt());
        assertEquals(updatedAt, userWithDates.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve criar usuário com username e email específicos")
    void shouldCreateUserWithSpecificUsernameAndEmail() {
        // Given
        String username = "joao.silva";
        String email = "joao.silva@exemplo.com";
        
        // When
        User userWithCredentials = UserTestBuilder.aUser()
            .withUsername(username)
            .withEmail(email)
            .buildDomain();
        
        // Then
        assertEquals(username, userWithCredentials.getUsername());
        assertEquals(email, userWithCredentials.getEmail());
    }
    
    @Test
    @DisplayName("Deve criar usuário com IDs externos específicos")
    void shouldCreateUserWithSpecificExternalIds() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        
        // When
        User userWithExternalIds = UserTestBuilder.aUser()
            .withIdUserExternal(idUserExternal)
            .withCodeUser(codeUser)
            .buildDomain();
        
        // Then
        assertEquals(idUserExternal, userWithExternalIds.getIdUserExternal());
        assertEquals(codeUser, userWithExternalIds.getCodeUser());
    }
    
    @Test
    @DisplayName("Deve verificar se usuário está ativo")
    void shouldCheckIfUserIsActive() {
        // Given
        User activeUser = UserTestBuilder.aUser()
            .asActive()
            .buildDomain();
        
        User blockedUser = UserTestBuilder.aUser()
            .asBlocked()
            .buildDomain();
        
        // Then
        assertEquals(UserStatus.ACTIVE, activeUser.getStatus());
        assertNotEquals(UserStatus.ACTIVE, blockedUser.getStatus());
    }
    
    @Test
    @DisplayName("Deve verificar se usuário tem email verificado")
    void shouldCheckIfUserHasVerifiedEmail() {
        // Given
        User verifiedUser = UserTestBuilder.aUser()
            .withEmailVerified(true)
            .buildDomain();
        
        User unverifiedUser = UserTestBuilder.aUser()
            .withEmailVerified(false)
            .buildDomain();
        
        // Then
        assertTrue(verifiedUser.getEmailVerified());
        assertFalse(unverifiedUser.getEmailVerified());
    }
    
    @Test
    @DisplayName("Deve criar usuário com hash de senha")
    void shouldCreateUserWithPasswordHash() {
        // Given
        String passwordHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUz0F4A0A0A0A0A0A0A0A0A0A";
        
        // When
        User userWithPassword = UserTestBuilder.aUser()
            .withPasswordHash(passwordHash)
            .buildDomain();
        
        // Then
        assertEquals(passwordHash, userWithPassword.getPasswordHash());
        assertNotNull(userWithPassword.getPasswordHash());
    }
}
