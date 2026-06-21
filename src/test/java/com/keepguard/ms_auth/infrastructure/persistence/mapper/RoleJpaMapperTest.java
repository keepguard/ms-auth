package com.keepguard.ms_auth.infrastructure.persistence.mapper;

import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role JPA Mapper Tests")
class RoleJpaMapperTest {

    private RoleJpaMapper mapper;
    private Role role;
    private RoleJpaEntity testRoleJpaEntity;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        mapper = new RoleJpaMapper();
        roleId = UUID.randomUUID();
        role = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN")
            .withDescription("Administrador do sistema")
            .withCreatedAt(LocalDateTime.now())
            .withUpdatedAt(LocalDateTime.now())
            .buildDomain();
            
        testRoleJpaEntity = mapper.toJpaEntity(role);
    }

    @Test
    @DisplayName("Deve converter Role domain para entidade JPA")
    void shouldConvertDomainToEntity() {
        // When
        RoleJpaEntity result = mapper.toJpaEntity(role);

        // Then
        assertNotNull(result);
        assertEquals(role.getId(), result.getId());
        assertEquals(role.getName(), result.getName());
        assertEquals(role.getDescription(), result.getDescription());
        assertEquals(role.getCreatedAt(), result.getCreatedAt());
        assertEquals(role.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve converter entidade JPA para Role domain")
    void shouldConvertEntityToDomain() {
        // When
        Role result = mapper.toDomain(testRoleJpaEntity);

        // Then
        assertNotNull(result);
        assertEquals(role.getId(), result.getId());
        assertEquals(role.getName(), result.getName());
        assertEquals(role.getDescription(), result.getDescription());
        assertEquals(role.getCreatedAt(), result.getCreatedAt());
        assertEquals(role.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve retornar null quando domain é null")
    void shouldReturnNullWhenDomainIsNull() {
        // When
        RoleJpaEntity result = mapper.toJpaEntity(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve retornar null quando entity é null")
    void shouldReturnNullWhenEntityIsNull() {
        // When
        Role result = mapper.toDomain(null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter Role com valores nulos")
    void shouldConvertRoleWithNullValues() {
        // Given
        Role roleWithNulls = Role.builder()
            .id(roleId)
            .name("TEST_ROLE")
            .description(null)
            .createdAt(null)
            .updatedAt(null)
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithNulls);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("TEST_ROLE", result.getName());
        assertNull(result.getDescription());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve converter Role com valores padrão")
    void shouldConvertRoleWithDefaultValues() {
        // Given
        Role roleWithDefaults = Role.builder()
            .id(roleId)
            .name("USER")
            .description("Usuário comum")
            .createdAt(LocalDateTime.of(2023, 1, 1, 10, 0))
            .updatedAt(LocalDateTime.of(2023, 1, 2, 15, 30))
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithDefaults);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("USER", result.getName());
        assertEquals("Usuário comum", result.getDescription());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), result.getCreatedAt());
        assertEquals(LocalDateTime.of(2023, 1, 2, 15, 30), result.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve converter Role com ID nulo")
    void shouldConvertRoleWithNullId() {
        // Given
        Role roleWithNullId = Role.builder()
            .id(null)
            .name("TEST_ROLE")
            .description("Test description")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithNullId);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("TEST_ROLE", result.getName());
        assertEquals("Test description", result.getDescription());
    }

    @Test
    @DisplayName("Deve converter Role com nome vazio")
    void shouldConvertRoleWithEmptyName() {
        // Given
        Role roleWithEmptyName = Role.builder()
            .id(roleId)
            .name("")
            .description("Test description")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithEmptyName);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("", result.getName());
        assertEquals("Test description", result.getDescription());
    }

    @Test
    @DisplayName("Deve converter Role com descrição vazia")
    void shouldConvertRoleWithEmptyDescription() {
        // Given
        Role roleWithEmptyDescription = Role.builder()
            .id(roleId)
            .name("TEST_ROLE")
            .description("")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithEmptyDescription);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("TEST_ROLE", result.getName());
        assertEquals("", result.getDescription());
    }

    @Test
    @DisplayName("Deve converter Role com timestamps específicos")
    void shouldConvertRoleWithSpecificTimestamps() {
        // Given
        LocalDateTime specificCreatedAt = LocalDateTime.of(2023, 6, 15, 9, 30, 45);
        LocalDateTime specificUpdatedAt = LocalDateTime.of(2023, 6, 16, 14, 20, 10);
        
        Role roleWithSpecificTimestamps = Role.builder()
            .id(roleId)
            .name("TIMESTAMPED_ROLE")
            .description("Role with specific timestamps")
            .createdAt(specificCreatedAt)
            .updatedAt(specificUpdatedAt)
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithSpecificTimestamps);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("TIMESTAMPED_ROLE", result.getName());
        assertEquals("Role with specific timestamps", result.getDescription());
        assertEquals(specificCreatedAt, result.getCreatedAt());
        assertEquals(specificUpdatedAt, result.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve converter Role com caracteres especiais no nome")
    void shouldConvertRoleWithSpecialCharactersInName() {
        // Given
        Role roleWithSpecialChars = Role.builder()
            .id(roleId)
            .name("ROLE_SPECIAL_CHARS_&_SYMBOLS")
            .description("Role com caracteres especiais: @#$%^&*()")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithSpecialChars);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("ROLE_SPECIAL_CHARS_&_SYMBOLS", result.getName());
        assertEquals("Role com caracteres especiais: @#$%^&*()", result.getDescription());
    }

    @Test
    @DisplayName("Deve converter Role com nome longo")
    void shouldConvertRoleWithLongName() {
        // Given
        String longName = "ROLE_VERY_LONG_NAME_THAT_EXCEEDS_NORMAL_LENGTH_FOR_TESTING_PURPOSES";
        Role roleWithLongName = Role.builder()
            .id(roleId)
            .name(longName)
            .description("Role with very long name")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithLongName);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals(longName, result.getName());
        assertEquals("Role with very long name", result.getDescription());
    }

    @Test
    @DisplayName("Deve converter Role com descrição longa")
    void shouldConvertRoleWithLongDescription() {
        // Given
        String longDescription = "Esta é uma descrição muito longa para testar o comportamento do mapper " +
            "quando a descrição contém muitos caracteres e pode exceder o tamanho normal de uma descrição " +
            "de role. Isso é importante para garantir que o mapper funcione corretamente com textos longos.";
        
        Role roleWithLongDescription = Role.builder()
            .id(roleId)
            .name("ROLE_LONG_DESC")
            .description(longDescription)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // When
        RoleJpaEntity result = mapper.toJpaEntity(roleWithLongDescription);

        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("ROLE_LONG_DESC", result.getName());
        assertEquals(longDescription, result.getDescription());
    }

    @Test
    @DisplayName("Deve converter Role com diferentes tipos de nomes")
    void shouldConvertRoleWithDifferentNameTypes() {
        // Given
        String[] roleNames = {"ADMIN", "USER", "MANAGER", "GUEST", "MODERATOR", "SUPER_ADMIN"};
        
        for (String roleName : roleNames) {
            Role testRole = Role.builder()
                .id(roleId)
                .name(roleName)
                .description("Description for " + roleName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            // When
            RoleJpaEntity result = mapper.toJpaEntity(testRole);

            // Then
            assertNotNull(result);
            assertEquals(roleId, result.getId());
            assertEquals(roleName, result.getName());
            assertEquals("Description for " + roleName, result.getDescription());
        }
    }
}
