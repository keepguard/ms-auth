package com.keepguard.ms_auth.domain.entity.user;

import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade PasswordHistory
 */
class PasswordHistoryTest {
    
    private PasswordHistory passwordHistory;
    private User user;
    private UUID historyId;
    
    @BeforeEach
    void setUp() {
        historyId = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .asActive()
            .buildDomain();
        
        passwordHistory = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUz0F4A0A0A0A0A0A0A0A0A0")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("Deve criar histórico de senha com dados válidos")
    void shouldCreatePasswordHistoryWithValidData() {
        // Then
        assertNotNull(passwordHistory.getId());
        assertEquals(user.getId(), passwordHistory.getUserId());
        assertEquals("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhUz0F4A0A0A0A0A0A0A0A0A0", passwordHistory.getPasswordHash());
        assertNotNull(passwordHistory.getCreatedAt());
    }
    
    @Test
    @DisplayName("Deve criar histórico com ID específico")
    void shouldCreatePasswordHistoryWithSpecificId() {
        // Given
        UUID specificId = UUID.randomUUID();
        
        // When
        PasswordHistory historyWithId = PasswordHistory.builder()
            .id(specificId)
                .userId(user.getId())
            .passwordHash("$2a$10$specific.hash.here")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(specificId, historyWithId.getId());
    }
    
    @Test
    @DisplayName("Deve criar histórico com hash de senha específico")
    void shouldCreatePasswordHistoryWithSpecificPasswordHash() {
        // Given
        String specificHash = "$2a$10$specific.password.hash.for.testing";
        
        // When
        PasswordHistory historyWithHash = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash(specificHash)
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(specificHash, historyWithHash.getPasswordHash());
    }
    
    @Test
    @DisplayName("Deve criar histórico com data específica")
    void shouldCreatePasswordHistoryWithSpecificDate() {
        // Given
        LocalDateTime specificDate = LocalDateTime.now().minusDays(30);
        
        // When
        PasswordHistory historyWithDate = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$hash.with.specific.date")
            .createdAt(specificDate)
            .build();
        
        // Then
        assertEquals(specificDate, historyWithDate.getCreatedAt());
    }
    
    @Test
    @DisplayName("Deve criar histórico com usuário específico")
    void shouldCreatePasswordHistoryWithSpecificUser() {
        // Given
        User specificUser = UserTestBuilder.aUser()
            .withUsername("specificuser")
            .withEmail("specific@example.com")
            .buildDomain();
        
        // When
        PasswordHistory historyWithUser = PasswordHistory.builder()
            .id(historyId)
            .userId(specificUser.getId())
            .passwordHash("$2a$10$hash.for.specific.user")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(specificUser.getId(), historyWithUser.getUserId());
    }
    
    @Test
    @DisplayName("Deve criar histórico com hash de senha longo")
    void shouldCreatePasswordHistoryWithLongPasswordHash() {
        // Given
        String longHash = "$2a$10$" + "A".repeat(100);
        
        // When
        PasswordHistory historyWithLongHash = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash(longHash)
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(longHash, historyWithLongHash.getPasswordHash());
    }
    
    @Test
    @DisplayName("Deve criar histórico com hash de senha curto")
    void shouldCreatePasswordHistoryWithShortPasswordHash() {
        // Given
        String shortHash = "$2a$10$short";
        
        // When
        PasswordHistory historyWithShortHash = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash(shortHash)
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(shortHash, historyWithShortHash.getPasswordHash());
    }
    
    @Test
    @DisplayName("Deve criar múltiplos históricos para o mesmo usuário")
    void shouldCreateMultiplePasswordHistoriesForSameUser() {
        // Given
        String hash1 = "$2a$10$first.password.hash";
        String hash2 = "$2a$10$second.password.hash";
        String hash3 = "$2a$10$third.password.hash";
        
        // When
        PasswordHistory history1 = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash(hash1)
            .createdAt(LocalDateTime.now().minusDays(3))
            .build();
        
        PasswordHistory history2 = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash(hash2)
            .createdAt(LocalDateTime.now().minusDays(2))
            .build();
        
        PasswordHistory history3 = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash(hash3)
            .createdAt(LocalDateTime.now().minusDays(1))
            .build();
        
        // Then
        assertEquals(user.getId(), history1.getUserId());
        assertEquals(hash1, history1.getPasswordHash());
        
        assertEquals(user.getId(), history2.getUserId());
        assertEquals(hash2, history2.getPasswordHash());
        
        assertEquals(user.getId(), history3.getUserId());
        assertEquals(hash3, history3.getPasswordHash());
    }
    
    @Test
    @DisplayName("Deve criar histórico com data no passado")
    void shouldCreatePasswordHistoryWithPastDate() {
        // Given
        LocalDateTime pastDate = LocalDateTime.now().minusDays(365);
        
        // When
        PasswordHistory historyWithPastDate = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$past.date.hash")
            .createdAt(pastDate)
            .build();
        
        // Then
        assertEquals(pastDate, historyWithPastDate.getCreatedAt());
        assertTrue(historyWithPastDate.getCreatedAt().isBefore(LocalDateTime.now()));
    }
    
    @Test
    @DisplayName("Deve criar histórico com data no futuro")
    void shouldCreatePasswordHistoryWithFutureDate() {
        // Given
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        
        // When
        PasswordHistory historyWithFutureDate = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$future.date.hash")
            .createdAt(futureDate)
            .build();
        
        // Then
        assertEquals(futureDate, historyWithFutureDate.getCreatedAt());
        assertTrue(historyWithFutureDate.getCreatedAt().isAfter(LocalDateTime.now()));
    }
    
    @Test
    @DisplayName("Deve verificar igualdade entre históricos")
    void shouldCheckEqualityBetweenPasswordHistories() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.now();
        PasswordHistory history1 = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$same.hash")
            .createdAt(fixedTime)
            .build();
        
        PasswordHistory history2 = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$same.hash")
            .createdAt(fixedTime)
            .build();
        
        PasswordHistory history3 = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash("$2a$10$different.hash")
            .createdAt(fixedTime)
            .build();
        
        // When & Then
        assertEquals(history1, history2);
        assertNotEquals(history1, history3);
    }
    
    @Test
    @DisplayName("Deve verificar hash code dos históricos")
    void shouldCheckHashCodeOfPasswordHistories() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.now();
        PasswordHistory history1 = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$same.hash")
            .createdAt(fixedTime)
            .build();
        
        PasswordHistory history2 = PasswordHistory.builder()
            .id(historyId)
                .userId(user.getId())
            .passwordHash("$2a$10$same.hash")
            .createdAt(fixedTime)
            .build();
        
        PasswordHistory history3 = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash("$2a$10$different.hash")
            .createdAt(fixedTime)
            .build();
        
        // When & Then
        assertEquals(history1.hashCode(), history2.hashCode());
        assertNotEquals(history1.hashCode(), history3.hashCode());
    }
    
    @Test
    @DisplayName("Deve criar histórico com diferentes algoritmos de hash")
    void shouldCreatePasswordHistoryWithDifferentHashAlgorithms() {
        // Given
        String bcryptHash = "$2a$10$bcrypt.hash.here";
        String scryptHash = "$scrypt$hash.here";
        String argon2Hash = "$argon2id$hash.here";
        
        // When
        PasswordHistory bcryptHistory = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash(bcryptHash)
            .createdAt(LocalDateTime.now())
            .build();
        
        PasswordHistory scryptHistory = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash(scryptHash)
            .createdAt(LocalDateTime.now())
            .build();
        
        PasswordHistory argon2History = PasswordHistory.builder()
            .id(UUID.randomUUID())
                .userId(user.getId())
            .passwordHash(argon2Hash)
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(bcryptHash, bcryptHistory.getPasswordHash());
        assertEquals(scryptHash, scryptHistory.getPasswordHash());
        assertEquals(argon2Hash, argon2History.getPasswordHash());
    }
}
