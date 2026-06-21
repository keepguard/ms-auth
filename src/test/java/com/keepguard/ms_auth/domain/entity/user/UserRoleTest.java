package com.keepguard.ms_auth.domain.entity.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade UserRole
 */
class UserRoleTest {
    
    private UserRole userRole;
    private UUID userId;
    private UUID roleId;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        roleId = UUID.randomUUID();
        
        userRole = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("Deve criar associação usuário-role com dados válidos")
    void shouldCreateUserRoleWithValidData() {
        // Then
        assertEquals(userId, userRole.getUserId());
        assertEquals(roleId, userRole.getRoleId());
        assertNotNull(userRole.getAssignedAt());
    }
    
    @Test
    @DisplayName("Deve criar associação com IDs específicos")
    void shouldCreateUserRoleWithSpecificIds() {
        // Given
        UUID specificUserId = UUID.randomUUID();
        UUID specificRoleId = UUID.randomUUID();
        
        // When
        UserRole specificUserRole = UserRole.builder()
            .userId(specificUserId)
            .roleId(specificRoleId)
            .assignedAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(specificUserId, specificUserRole.getUserId());
        assertEquals(specificRoleId, specificUserRole.getRoleId());
    }
    
    @Test
    @DisplayName("Deve criar associação com data específica")
    void shouldCreateUserRoleWithSpecificDate() {
        // Given
        LocalDateTime specificDate = LocalDateTime.now().minusDays(30);
        
        // When
        UserRole userRoleWithDate = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(specificDate)
            .build();
        
        // Then
        assertEquals(specificDate, userRoleWithDate.getAssignedAt());
    }
    
    @Test
    @DisplayName("Deve criar associação com data atual")
    void shouldCreateUserRoleWithCurrentDate() {
        // Given
        LocalDateTime beforeCreation = LocalDateTime.now();
        
        // When
        UserRole userRoleWithCurrentDate = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(LocalDateTime.now())
            .build();
        
        // Then
        assertNotNull(userRoleWithCurrentDate.getAssignedAt());
        assertTrue(userRoleWithCurrentDate.getAssignedAt().isAfter(beforeCreation.minusSeconds(1)));
        assertTrue(userRoleWithCurrentDate.getAssignedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    @DisplayName("Deve criar associação com data nula")
    void shouldCreateUserRoleWithNullDate() {
        // When
        UserRole userRoleWithNullDate = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(null)
            .build();
        
        // Then
        assertEquals(userId, userRoleWithNullDate.getUserId());
        assertEquals(roleId, userRoleWithNullDate.getRoleId());
        assertNull(userRoleWithNullDate.getAssignedAt());
    }
    
    @Test
    @DisplayName("Deve criar múltiplas associações para o mesmo usuário")
    void shouldCreateMultipleUserRolesForSameUser() {
        // Given
        UUID roleId1 = UUID.randomUUID();
        UUID roleId2 = UUID.randomUUID();
        UUID roleId3 = UUID.randomUUID();
        
        // When
        UserRole userRole1 = UserRole.builder()
            .userId(userId)
            .roleId(roleId1)
            .assignedAt(LocalDateTime.now().minusDays(3))
            .build();
        
        UserRole userRole2 = UserRole.builder()
            .userId(userId)
            .roleId(roleId2)
            .assignedAt(LocalDateTime.now().minusDays(2))
            .build();
        
        UserRole userRole3 = UserRole.builder()
            .userId(userId)
            .roleId(roleId3)
            .assignedAt(LocalDateTime.now().minusDays(1))
            .build();
        
        // Then
        assertEquals(userId, userRole1.getUserId());
        assertEquals(roleId1, userRole1.getRoleId());
        
        assertEquals(userId, userRole2.getUserId());
        assertEquals(roleId2, userRole2.getRoleId());
        
        assertEquals(userId, userRole3.getUserId());
        assertEquals(roleId3, userRole3.getRoleId());
    }
    
    @Test
    @DisplayName("Deve criar múltiplas associações para a mesma role")
    void shouldCreateMultipleUserRolesForSameRole() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        
        // When
        UserRole userRole1 = UserRole.builder()
            .userId(userId1)
            .roleId(roleId)
            .assignedAt(LocalDateTime.now().minusDays(3))
            .build();
        
        UserRole userRole2 = UserRole.builder()
            .userId(userId2)
            .roleId(roleId)
            .assignedAt(LocalDateTime.now().minusDays(2))
            .build();
        
        UserRole userRole3 = UserRole.builder()
            .userId(userId3)
            .roleId(roleId)
            .assignedAt(LocalDateTime.now().minusDays(1))
            .build();
        
        // Then
        assertEquals(userId1, userRole1.getUserId());
        assertEquals(roleId, userRole1.getRoleId());
        
        assertEquals(userId2, userRole2.getUserId());
        assertEquals(roleId, userRole2.getRoleId());
        
        assertEquals(userId3, userRole3.getUserId());
        assertEquals(roleId, userRole3.getRoleId());
    }
    
    @Test
    @DisplayName("Deve verificar igualdade entre associações")
    void shouldCheckEqualityBetweenUserRoles() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.now();
        UserRole userRole1 = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(fixedTime)
            .build();
        
        UserRole userRole2 = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(fixedTime)
            .build();
        
        UserRole userRole3 = UserRole.builder()
            .userId(UUID.randomUUID())
            .roleId(roleId)
            .assignedAt(fixedTime)
            .build();
        
        // When & Then
        assertEquals(userRole1, userRole2);
        assertNotEquals(userRole1, userRole3);
    }
    
    @Test
    @DisplayName("Deve verificar hash code das associações")
    void shouldCheckHashCodeOfUserRoles() {
        // Given
        LocalDateTime fixedTime = LocalDateTime.now();
        UserRole userRole1 = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(fixedTime)
            .build();
        
        UserRole userRole2 = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(fixedTime)
            .build();
        
        UserRole userRole3 = UserRole.builder()
            .userId(UUID.randomUUID())
            .roleId(roleId)
            .assignedAt(fixedTime)
            .build();
        
        // When & Then
        assertEquals(userRole1.hashCode(), userRole2.hashCode());
        assertNotEquals(userRole1.hashCode(), userRole3.hashCode());
    }
    
    @Test
    @DisplayName("Deve criar associação com data no passado")
    void shouldCreateUserRoleWithPastDate() {
        // Given
        LocalDateTime pastDate = LocalDateTime.now().minusDays(365);
        
        // When
        UserRole userRoleWithPastDate = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(pastDate)
            .build();
        
        // Then
        assertEquals(pastDate, userRoleWithPastDate.getAssignedAt());
        assertTrue(userRoleWithPastDate.getAssignedAt().isBefore(LocalDateTime.now()));
    }
    
    @Test
    @DisplayName("Deve criar associação com data no futuro")
    void shouldCreateUserRoleWithFutureDate() {
        // Given
        LocalDateTime futureDate = LocalDateTime.now().plusDays(30);
        
        // When
        UserRole userRoleWithFutureDate = UserRole.builder()
            .userId(userId)
            .roleId(roleId)
            .assignedAt(futureDate)
            .build();
        
        // Then
        assertEquals(futureDate, userRoleWithFutureDate.getAssignedAt());
        assertTrue(userRoleWithFutureDate.getAssignedAt().isAfter(LocalDateTime.now()));
    }
}
