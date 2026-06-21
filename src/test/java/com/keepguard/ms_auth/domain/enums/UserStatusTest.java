package com.keepguard.ms_auth.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum UserStatus
 */
class UserStatusTest {
    
    @Test
    @DisplayName("Deve conter todos os valores esperados")
    void shouldContainAllExpectedValues() {
        // Given & When
        UserStatus[] values = UserStatus.values();
        
        // Then
        assertEquals(3, values.length);
        assertTrue(containsValue(values, UserStatus.ACTIVE));
        assertTrue(containsValue(values, UserStatus.BLOCKED));
        assertTrue(containsValue(values, UserStatus.DELETED));
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para ACTIVE")
    void shouldReturnCorrectValueForActive() {
        // When
        UserStatus status = UserStatus.ACTIVE;
        
        // Then
        assertEquals("ACTIVE", status.name());
        assertEquals(0, status.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para BLOCKED")
    void shouldReturnCorrectValueForBlocked() {
        // When
        UserStatus status = UserStatus.BLOCKED;
        
        // Then
        assertEquals("BLOCKED", status.name());
        assertEquals(1, status.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para DELETED")
    void shouldReturnCorrectValueForDeleted() {
        // When
        UserStatus status = UserStatus.DELETED;
        
        // Then
        assertEquals("DELETED", status.name());
        assertEquals(2, status.ordinal());
    }
    
    @Test
    @DisplayName("Deve converter string para enum corretamente")
    void shouldConvertStringToEnumCorrectly() {
        // When & Then
        assertEquals(UserStatus.ACTIVE, UserStatus.valueOf("ACTIVE"));
        assertEquals(UserStatus.BLOCKED, UserStatus.valueOf("BLOCKED"));
        assertEquals(UserStatus.DELETED, UserStatus.valueOf("DELETED"));
    }
    
    @Test
    @DisplayName("Deve lançar exceção para string inválida")
    void shouldThrowExceptionForInvalidString() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            UserStatus.valueOf("INVALID");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção para string nula")
    void shouldThrowExceptionForNullString() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            UserStatus.valueOf(null);
        });
    }
    
    @Test
    @DisplayName("Deve verificar igualdade entre enums")
    void shouldCheckEqualityBetweenEnums() {
        // Given
        UserStatus status1 = UserStatus.ACTIVE;
        UserStatus status2 = UserStatus.ACTIVE;
        UserStatus status3 = UserStatus.BLOCKED;
        
        // When & Then
        assertEquals(status1, status2);
        assertNotEquals(status1, status3);
        assertSame(status1, status2);
        assertNotSame(status1, status3);
    }
    
    @Test
    @DisplayName("Deve verificar hash code dos enums")
    void shouldCheckHashCodeOfEnums() {
        // Given
        UserStatus status1 = UserStatus.ACTIVE;
        UserStatus status2 = UserStatus.ACTIVE;
        UserStatus status3 = UserStatus.BLOCKED;
        
        // When & Then
        assertEquals(status1.hashCode(), status2.hashCode());
        assertNotEquals(status1.hashCode(), status3.hashCode());
    }
    
    @Test
    @DisplayName("Deve verificar toString dos enums")
    void shouldCheckToStringOfEnums() {
        // When & Then
        assertEquals("ACTIVE", UserStatus.ACTIVE.toString());
        assertEquals("BLOCKED", UserStatus.BLOCKED.toString());
        assertEquals("DELETED", UserStatus.DELETED.toString());
    }
    
    @Test
    @DisplayName("Deve verificar compareTo dos enums")
    void shouldCheckCompareToOfEnums() {
        // When & Then
        assertTrue(UserStatus.ACTIVE.compareTo(UserStatus.BLOCKED) < 0);
        assertTrue(UserStatus.BLOCKED.compareTo(UserStatus.DELETED) < 0);
        assertTrue(UserStatus.ACTIVE.compareTo(UserStatus.DELETED) < 0);
        assertEquals(0, UserStatus.ACTIVE.compareTo(UserStatus.ACTIVE));
    }
    
    private boolean containsValue(UserStatus[] values, UserStatus target) {
        for (UserStatus value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
