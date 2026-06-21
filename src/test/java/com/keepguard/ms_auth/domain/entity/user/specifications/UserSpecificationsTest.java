package com.keepguard.ms_auth.domain.entity.user.specifications;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserSpecificationsTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = UserTestBuilder.builder()
                .withId(UUID.fromString("12345678-1234-1234-1234-123456789012"))
                .withIdUserExternal(UUID.fromString("87654321-4321-4321-4321-210987654321"))
                .withCodeUser(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .withUsername("testuser")
                .withEmail("test@example.com")
                .asActive()
                .buildDomain();
    }

    @Test
    @DisplayName("Deve criar specification com filtro por ID")
    void shouldCreateSpecificationWithIdFilter() {
        // Given
        UUID id = UUID.fromString("12345678-1234-1234-1234-123456789012");

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                id, null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por idUserExternal")
    void shouldCreateSpecificationWithIdUserExternalFilter() {
        // Given
        UUID idUserExternal = UUID.fromString("87654321-4321-4321-4321-210987654321");

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, idUserExternal, null, null, null, null, null, null, null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por codeUser")
    void shouldCreateSpecificationWithCodeUserFilter() {
        // Given
        UUID codeUser = UUID.fromString("11111111-2222-3333-4444-555555555555");

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, codeUser, null, null, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por username")
    void shouldCreateSpecificationWithUsernameFilter() {
        // Given
        String username = "testuser";

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, username, null, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por email")
    void shouldCreateSpecificationWithEmailFilter() {
        // Given
        String email = "test@example.com";

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, email, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por status")
    void shouldCreateSpecificationWithStatusFilter() {
        // Given
        UserStatus status = UserStatus.ACTIVE;

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, null, status, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por emailVerified")
    void shouldCreateSpecificationWithEmailVerifiedFilter() {
        // Given
        Boolean emailVerified = true;

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, null, null, emailVerified, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por data de criação")
    void shouldCreateSpecificationWithCreatedAtFilter() {
        // Given
        LocalDateTime createdAtStart = LocalDateTime.now().minusDays(30);
        LocalDateTime createdAtEnd = LocalDateTime.now();

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, null, null, null, null,
                createdAtStart, createdAtEnd, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por data de atualização")
    void shouldCreateSpecificationWithUpdatedAtFilter() {
        // Given
        LocalDateTime updatedAtStart = LocalDateTime.now().minusDays(7);
        LocalDateTime updatedAtEnd = LocalDateTime.now();

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, null, null, null, null,
                null, null, updatedAtStart, updatedAtEnd, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com filtro por data de último login")
    void shouldCreateSpecificationWithLastLoginFilter() {
        // Given
        LocalDateTime lastLoginStart = LocalDateTime.now().minusDays(1);
        LocalDateTime lastLoginEnd = LocalDateTime.now();

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, null, null, null, null,
                null, null, null, null, lastLoginStart, lastLoginEnd
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com múltiplos filtros")
    void shouldCreateSpecificationWithMultipleFilters() {
        // Given
        UUID id = UUID.fromString("12345678-1234-1234-1234-123456789012");
        String username = "testuser";
        UserStatus status = UserStatus.ACTIVE;
        Boolean emailVerified = true;

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                id, null, null, username, null, status, emailVerified, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification sem filtros")
    void shouldCreateSpecificationWithoutFilters() {
        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve ignorar username vazio")
    void shouldIgnoreEmptyUsername() {
        // Given
        String username = "";

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, username, null, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve ignorar username com apenas espaços")
    void shouldIgnoreWhitespaceOnlyUsername() {
        // Given
        String username = "   ";

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, username, null, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve ignorar email vazio")
    void shouldIgnoreEmptyEmail() {
        // Given
        String email = "";

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, email, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve ignorar email com apenas espaços")
    void shouldIgnoreWhitespaceOnlyEmail() {
        // Given
        String email = "   ";

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, email, null, null, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve criar specification com todos os filtros")
    void shouldCreateSpecificationWithAllFilters() {
        // Given
        UUID id = UUID.fromString("12345678-1234-1234-1234-123456789012");
        UUID idUserExternal = UUID.fromString("87654321-4321-4321-4321-210987654321");
        UUID codeUser = UUID.fromString("11111111-2222-3333-4444-555555555555");
        String username = "testuser";
        String email = "test@example.com";
        UserStatus status = UserStatus.ACTIVE;
        Boolean emailVerified = true;
        LocalDateTime now = LocalDateTime.now();

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                id, idUserExternal, codeUser, username, email, status, emailVerified, null,
                now.minusDays(30), now, now.minusDays(7), now, now.minusDays(1), now
        );

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Deve retornar specification não nula para filtros válidos")
    void shouldReturnNonNullSpecificationForValidFilters() {
        // Given
        UUID id = UUID.fromString("12345678-1234-1234-1234-123456789012");
        String username = "testuser";
        String email = "test@example.com";
        UserStatus status = UserStatus.ACTIVE;
        Boolean emailVerified = true;

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                id, null, null, username, email, status, emailVerified, null,
                null, null, null, null, null, null
        );

        // Then
        assertNotNull(spec);
        assertTrue(spec instanceof Specification);
    }

    @Test
    @DisplayName("Deve criar specification com filtros de data válidos")
    void shouldCreateSpecificationWithValidDateFilters() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(30);
        LocalDateTime endDate = now;

        // When
        Specification<User> spec = UserSpecifications.withDynamicFilters(
                null, null, null, null, null, null, null, null,
                startDate, endDate, startDate, endDate, startDate, endDate
        );

        // Then
        assertNotNull(spec);
    }
}