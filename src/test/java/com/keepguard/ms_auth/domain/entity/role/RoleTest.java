package com.keepguard.ms_auth.domain.entity.role;

import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Role
 */
class RoleTest {
    
    private Role role;
    private UUID roleId;
    
    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        
        role = RoleTestBuilder.aRole()
            .withId(roleId)
            .buildDomain();
    }
    
    @Test
    @DisplayName("Deve criar role com dados válidos")
    void shouldCreateRoleWithValidData() {
        // Then
        assertNotNull(role.getId());
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertNotNull(role.getCreatedAt());
        assertNotNull(role.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve criar role com ID específico")
    void shouldCreateRoleWithSpecificId() {
        // Given
        UUID specificId = UUID.randomUUID();
        
        // When
        Role roleWithId = RoleTestBuilder.aRole()
            .withId(specificId)
            .buildDomain();
        
        // Then
        assertEquals(specificId, roleWithId.getId());
    }
    
    @Test
    @DisplayName("Deve criar role de administrador")
    void shouldCreateAdminRole() {
        // When
        Role adminRole = RoleTestBuilder.aRole()
            .withName("ADMIN")
            .withDescription("Administrador do sistema")
            .buildDomain();
        
        // Then
        assertEquals("ADMIN", adminRole.getName());
        assertEquals("Administrador do sistema", adminRole.getDescription());
    }
    
    @Test
    @DisplayName("Deve criar role de usuário")
    void shouldCreateUserRole() {
        // When
        Role userRole = RoleTestBuilder.aRole()
            .withName("USER")
            .withDescription("Usuário comum")
            .buildDomain();
        
        // Then
        assertEquals("USER", userRole.getName());
        assertEquals("Usuário comum", userRole.getDescription());
    }
    
    @Test
    @DisplayName("Deve criar role de gerente")
    void shouldCreateManagerRole() {
        // When
        Role managerRole = RoleTestBuilder.aRole()
            .withName("MANAGER")
            .withDescription("Gerente")
            .buildDomain();
        
        // Then
        assertEquals("MANAGER", managerRole.getName());
        assertEquals("Gerente", managerRole.getDescription());
    }
    
    @Test
    @DisplayName("Deve criar role com nome específico")
    void shouldCreateRoleWithSpecificName() {
        // Given
        String name = "ROLE_CUSTOM";
        
        // When
        Role roleWithName = RoleTestBuilder.aRole()
            .withName(name)
            .buildDomain();
        
        // Then
        assertEquals(name, roleWithName.getName());
    }
    
    @Test
    @DisplayName("Deve criar role com descrição específica")
    void shouldCreateRoleWithSpecificDescription() {
        // Given
        String description = "Custom role for specific permissions";
        
        // When
        Role roleWithDescription = RoleTestBuilder.aRole()
            .withDescription(description)
            .buildDomain();
        
        // Then
        assertEquals(description, roleWithDescription.getDescription());
    }
    
    @Test
    @DisplayName("Deve criar role com datas específicas")
    void shouldCreateRoleWithSpecificDates() {
        // Given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(30);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        
        // When
        Role roleWithDates = RoleTestBuilder.aRole()
            .withCreatedAt(createdAt)
            .withUpdatedAt(updatedAt)
            .buildDomain();
        
        // Then
        assertEquals(createdAt, roleWithDates.getCreatedAt());
        assertEquals(updatedAt, roleWithDates.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve criar role sem descrição")
    void shouldCreateRoleWithoutDescription() {
        // When
        Role roleWithoutDescription = RoleTestBuilder.aRole()
            .withDescription(null)
            .buildDomain();
        
        // Then
        assertNull(roleWithoutDescription.getDescription());
    }
    
    @Test
    @DisplayName("Deve criar role com valores padrão corretos")
    void shouldCreateRoleWithCorrectDefaultValues() {
        // When
        Role defaultRole = Role.builder()
            .name("ROLE_TEST")
            .build();
        
        // Then
        // ID é gerado pelo JPA, não pelo builder
        assertEquals("ROLE_TEST", defaultRole.getName());
        assertNull(defaultRole.getDescription());
        // createdAt e updatedAt podem ser null no builder, são definidos pelo JPA
    }
    
    @Test
    @DisplayName("Deve criar role com nome vazio")
    void shouldCreateRoleWithEmptyName() {
        // When
        Role roleWithEmptyName = RoleTestBuilder.aRole()
            .withName("")
            .buildDomain();
        
        // Then
        assertEquals("", roleWithEmptyName.getName());
    }
    
    @Test
    @DisplayName("Deve criar role com descrição vazia")
    void shouldCreateRoleWithEmptyDescription() {
        // When
        Role roleWithEmptyDescription = RoleTestBuilder.aRole()
            .withDescription("")
            .buildDomain();
        
        // Then
        assertEquals("", roleWithEmptyDescription.getDescription());
    }
    
    @Test
    @DisplayName("Deve criar role com nome muito longo")
    void shouldCreateRoleWithLongName() {
        // Given
        String longName = "ROLE_" + "A".repeat(100);
        
        // When
        Role roleWithLongName = RoleTestBuilder.aRole()
            .withName(longName)
            .buildDomain();
        
        // Then
        assertEquals(longName, roleWithLongName.getName());
    }
    
    @Test
    @DisplayName("Deve criar role com descrição muito longa")
    void shouldCreateRoleWithLongDescription() {
        // Given
        String longDescription = "A".repeat(1000);
        
        // When
        Role roleWithLongDescription = RoleTestBuilder.aRole()
            .withDescription(longDescription)
            .buildDomain();
        
        // Then
        assertEquals(longDescription, roleWithLongDescription.getDescription());
    }
}
