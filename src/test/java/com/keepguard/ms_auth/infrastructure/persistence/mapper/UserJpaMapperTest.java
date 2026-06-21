package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserJpaEntity;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UserJpaMapper
 * Testa conversão bidirecional entre User domain e User JPA Entity
 */
class UserJpaMapperTest {

    private UserJpaMapper userJpaMapper;
    private User testUser;
    private UserJpaEntity testUserJpaEntity;
    private UUID userId;
    private UUID idUserExternal;
    private UUID codeUser;

    @BeforeEach
    void setUp() {
        userJpaMapper = new UserJpaMapper();
        
        userId = UUID.randomUUID();
        idUserExternal = UUID.randomUUID();
        codeUser = UUID.randomUUID();
        
        testUser = UserTestBuilder.aUser()
            .withId(userId)
            .withIdUserExternal(idUserExternal)
            .withCodeUser(codeUser)
            .withUsername("testuser")
            .withEmail("test@example.com")
            .withPasswordHash("hashedpassword")
            .asActive()
            .withLastLogin(LocalDateTime.now())
            .buildDomain();
            
        testUserJpaEntity = userJpaMapper.toJpaEntity(testUser);
    }

    @Test
    @DisplayName("Deve converter User domain para User entity com sucesso")
    void shouldConvertDomainToEntitySuccessfully() {
        // When
        UserJpaEntity entity = userJpaMapper.toJpaEntity(testUser);

        // Then
        assertNotNull(entity);
        assertEquals(testUser.getId(), entity.getId());
        assertEquals(testUser.getIdUserExternal(), entity.getIdUserExternal());
        assertEquals(testUser.getCodeUser(), entity.getCodeUser());
        assertEquals(testUser.getUsername(), entity.getUsername());
        assertEquals(testUser.getEmail(), entity.getEmail());
        assertEquals(testUser.getPasswordHash(), entity.getPasswordHash());
        assertEquals(testUser.getStatus(), entity.getStatus());
        assertEquals(testUser.getEmailVerified(), entity.getEmailVerified());
        assertEquals(testUser.getCreatedAt(), entity.getCreatedAt());
        assertEquals(testUser.getUpdatedAt(), entity.getUpdatedAt());
        assertEquals(testUser.getLastLogin(), entity.getLastLogin());
    }

    @Test
    @DisplayName("Deve converter User entity para User domain com sucesso")
    void shouldConvertEntityToDomainSuccessfully() {
        // When
        User domain = userJpaMapper.toDomain(testUserJpaEntity);

        // Then
        assertNotNull(domain);
        assertEquals(testUser.getId(), domain.getId());
        assertEquals(testUser.getIdUserExternal(), domain.getIdUserExternal());
        assertEquals(testUser.getCodeUser(), domain.getCodeUser());
        assertEquals(testUser.getUsername(), domain.getUsername());
        assertEquals(testUser.getEmail(), domain.getEmail());
        assertEquals(testUser.getPasswordHash(), domain.getPasswordHash());
        assertEquals(testUser.getStatus(), domain.getStatus());
        assertEquals(testUser.getEmailVerified(), domain.getEmailVerified());
        assertEquals(testUser.getCreatedAt(), domain.getCreatedAt());
        assertEquals(testUser.getUpdatedAt(), domain.getUpdatedAt());
        assertEquals(testUser.getLastLogin(), domain.getLastLogin());
    }

    @Test
    @DisplayName("Deve retornar null quando converter domain null para entity")
    void shouldReturnNullWhenConvertingNullDomainToEntity() {
        // When
        UserJpaEntity entity = userJpaMapper.toJpaEntity(null);

        // Then
        assertNull(entity);
    }

    @Test
    @DisplayName("Deve retornar null quando converter entity null para domain")
    void shouldReturnNullWhenConvertingNullEntityToDomain() {
        // When
        User domain = userJpaMapper.toDomain(null);

        // Then
        assertNull(domain);
    }

    @Test
    @DisplayName("Deve manter consistência na conversão bidirecional")
    void shouldMaintainConsistencyInBidirectionalConversion() {
        // Given
        User originalUser = testUser;

        // When - Domain to Entity
        UserJpaEntity entity = userJpaMapper.toJpaEntity(originalUser);
        
        // When - Entity to Domain
        User convertedBack = userJpaMapper.toDomain(entity);

        // Then
        assertNotNull(entity);
        assertNotNull(convertedBack);
        
        // Verificar se os dados foram preservados
        assertEquals(originalUser.getId(), entity.getId());
        assertEquals(originalUser.getId(), convertedBack.getId());
        assertEquals(originalUser.getUsername(), entity.getUsername());
        assertEquals(originalUser.getUsername(), convertedBack.getUsername());
        assertEquals(originalUser.getEmail(), entity.getEmail());
        assertEquals(originalUser.getEmail(), convertedBack.getEmail());
        assertEquals(originalUser.getStatus(), entity.getStatus());
        assertEquals(originalUser.getStatus(), convertedBack.getStatus());
    }

    @Test
    @DisplayName("Deve converter User com todos os campos preenchidos")
    void shouldConvertUserWithAllFieldsFilled() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        User userWithAllFields = User.builder()
            .id(userId)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username("completeuser")
            .email("complete@example.com")
            .passwordHash("completepassword")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(now)
            .updatedAt(now)
            .lastLogin(now)
            .build();

        // When
        UserJpaEntity entity = userJpaMapper.toJpaEntity(userWithAllFields);
        User domain = userJpaMapper.toDomain(entity);

        // Then
        assertNotNull(entity);
        assertNotNull(domain);
        assertEquals(userWithAllFields.getId(), entity.getId());
        assertEquals(userWithAllFields.getId(), domain.getId());
        assertEquals("completeuser", entity.getUsername());
        assertEquals("completeuser", domain.getUsername());
        assertEquals("complete@example.com", entity.getEmail());
        assertEquals("complete@example.com", domain.getEmail());
        assertEquals(UserStatus.ACTIVE, entity.getStatus());
        assertEquals(UserStatus.ACTIVE, domain.getStatus());
        assertTrue(entity.getEmailVerified());
        assertTrue(domain.getEmailVerified());
        assertEquals(now, entity.getCreatedAt());
        assertEquals(now, domain.getCreatedAt());
        assertEquals(now, entity.getLastLogin());
        assertEquals(now, domain.getLastLogin());
    }

    @Test
    @DisplayName("Deve converter User com campos nulos")
    void shouldConvertUserWithNullFields() {
        // Given
        User userWithNulls = User.builder()
            .id(userId)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username("nulluser")
            .email("null@example.com")
            .passwordHash("nullpassword")
            .status(UserStatus.ACTIVE)
            .emailVerified(false)
            .createdAt(null)
            .updatedAt(null)
            .lastLogin(null)
            .build();

        // When
        UserJpaEntity entity = userJpaMapper.toJpaEntity(userWithNulls);
        User domain = userJpaMapper.toDomain(entity);

        // Then
        assertNotNull(entity);
        assertNotNull(domain);
        assertEquals(userWithNulls.getId(), entity.getId());
        assertEquals(userWithNulls.getId(), domain.getId());
        assertEquals("nulluser", entity.getUsername());
        assertEquals("nulluser", domain.getUsername());
        assertNull(entity.getCreatedAt());
        assertNull(domain.getCreatedAt());
        assertNull(entity.getLastLogin());
        assertNull(domain.getLastLogin());
    }

    @Test
    @DisplayName("Deve converter User com diferentes status")
    void shouldConvertUserWithDifferentStatus() {
        // Given
        UserStatus[] statuses = {UserStatus.ACTIVE, UserStatus.BLOCKED, UserStatus.DELETED};
        
        for (UserStatus status : statuses) {
            User userWithStatus = User.builder()
                .id(UUID.randomUUID())
                .idUserExternal(UUID.randomUUID())
                .codeUser(UUID.randomUUID())
                .username("user" + status.name().toLowerCase())
                .email("user" + status.name().toLowerCase() + "@example.com")
                .passwordHash("password")
                .status(status)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(null)
                .build();

            // When
            UserJpaEntity entity = userJpaMapper.toJpaEntity(userWithStatus);
            User domain = userJpaMapper.toDomain(entity);

            // Then
            assertNotNull(entity);
            assertNotNull(domain);
            assertEquals(status, entity.getStatus());
            assertEquals(status, domain.getStatus());
        }
    }

    @Test
    @DisplayName("Deve converter User com email verificado e não verificado")
    void shouldConvertUserWithVerifiedAndUnverifiedEmail() {
        // Given
        Boolean[] emailVerifiedValues = {true, false, null};
        
        for (Boolean emailVerified : emailVerifiedValues) {
            User userWithEmailStatus = User.builder()
                .id(UUID.randomUUID())
                .idUserExternal(UUID.randomUUID())
                .codeUser(UUID.randomUUID())
                .username("user" + emailVerified)
                .email("user" + emailVerified + "@example.com")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .emailVerified(emailVerified)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(null)
                .build();

            // When
            UserJpaEntity entity = userJpaMapper.toJpaEntity(userWithEmailStatus);
            User domain = userJpaMapper.toDomain(entity);

            // Then
            assertNotNull(entity);
            assertNotNull(domain);
            assertEquals(emailVerified, entity.getEmailVerified());
            assertEquals(emailVerified, domain.getEmailVerified());
        }
    }
}
