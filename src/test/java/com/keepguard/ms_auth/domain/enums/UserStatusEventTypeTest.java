package com.keepguard.ms_auth.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o enum UserStatusEventType
 */
class UserStatusEventTypeTest {
    
    @Test
    @DisplayName("Deve conter todos os valores esperados")
    void shouldContainAllExpectedValues() {
        // Given & When
        UserStatusEventType[] values = UserStatusEventType.values();
        
        // Then
        assertEquals(7, values.length);
        assertTrue(containsValue(values, UserStatusEventType.CREATED));
        assertTrue(containsValue(values, UserStatusEventType.BLOCKED));
        assertTrue(containsValue(values, UserStatusEventType.UNLOCKED));
        assertTrue(containsValue(values, UserStatusEventType.DELETED));
        assertTrue(containsValue(values, UserStatusEventType.EMAIL_VALIDATED));
        assertTrue(containsValue(values, UserStatusEventType.EMAIL_UPDATED));
        assertTrue(containsValue(values, UserStatusEventType.EMAIL_CHANGED));
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para CREATED")
    void shouldReturnCorrectValueForCreated() {
        // When
        UserStatusEventType eventType = UserStatusEventType.CREATED;
        
        // Then
        assertEquals("CREATED", eventType.name());
        assertEquals(0, eventType.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para BLOCKED")
    void shouldReturnCorrectValueForBlocked() {
        // When
        UserStatusEventType eventType = UserStatusEventType.BLOCKED;
        
        // Then
        assertEquals("BLOCKED", eventType.name());
        assertEquals(1, eventType.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para UNLOCKED")
    void shouldReturnCorrectValueForUnlocked() {
        // When
        UserStatusEventType eventType = UserStatusEventType.UNLOCKED;
        
        // Then
        assertEquals("UNLOCKED", eventType.name());
        assertEquals(2, eventType.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para DELETED")
    void shouldReturnCorrectValueForDeleted() {
        // When
        UserStatusEventType eventType = UserStatusEventType.DELETED;
        
        // Then
        assertEquals("DELETED", eventType.name());
        assertEquals(3, eventType.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para EMAIL_VALIDATED")
    void shouldReturnCorrectValueForEmailValidated() {
        // When
        UserStatusEventType eventType = UserStatusEventType.EMAIL_VALIDATED;
        
        // Then
        assertEquals("EMAIL_VALIDATED", eventType.name());
        assertEquals(4, eventType.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para EMAIL_UPDATED")
    void shouldReturnCorrectValueForEmailUpdated() {
        // When
        UserStatusEventType eventType = UserStatusEventType.EMAIL_UPDATED;
        
        // Then
        assertEquals("EMAIL_UPDATED", eventType.name());
        assertEquals(5, eventType.ordinal());
    }
    
    @Test
    @DisplayName("Deve retornar valor correto para EMAIL_CHANGED")
    void shouldReturnCorrectValueForEmailChanged() {
        // When
        UserStatusEventType eventType = UserStatusEventType.EMAIL_CHANGED;
        
        // Then
        assertEquals("EMAIL_CHANGED", eventType.name());
        assertEquals(6, eventType.ordinal());
    }
    
    @Test
    @DisplayName("Deve converter string para enum corretamente")
    void shouldConvertStringToEnumCorrectly() {
        // When & Then
        assertEquals(UserStatusEventType.CREATED, UserStatusEventType.valueOf("CREATED"));
        assertEquals(UserStatusEventType.BLOCKED, UserStatusEventType.valueOf("BLOCKED"));
        assertEquals(UserStatusEventType.UNLOCKED, UserStatusEventType.valueOf("UNLOCKED"));
        assertEquals(UserStatusEventType.DELETED, UserStatusEventType.valueOf("DELETED"));
        assertEquals(UserStatusEventType.EMAIL_VALIDATED, UserStatusEventType.valueOf("EMAIL_VALIDATED"));
        assertEquals(UserStatusEventType.EMAIL_UPDATED, UserStatusEventType.valueOf("EMAIL_UPDATED"));
        assertEquals(UserStatusEventType.EMAIL_CHANGED, UserStatusEventType.valueOf("EMAIL_CHANGED"));
    }
    
    @Test
    @DisplayName("Deve lançar exceção para string inválida")
    void shouldThrowExceptionForInvalidString() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            UserStatusEventType.valueOf("INVALID");
        });
    }
    
    @Test
    @DisplayName("Deve lançar exceção para string nula")
    void shouldThrowExceptionForNullString() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            UserStatusEventType.valueOf(null);
        });
    }
    
    @Test
    @DisplayName("Deve verificar igualdade entre enums")
    void shouldCheckEqualityBetweenEnums() {
        // Given
        UserStatusEventType eventType1 = UserStatusEventType.CREATED;
        UserStatusEventType eventType2 = UserStatusEventType.CREATED;
        UserStatusEventType eventType3 = UserStatusEventType.BLOCKED;
        
        // When & Then
        assertEquals(eventType1, eventType2);
        assertNotEquals(eventType1, eventType3);
        assertSame(eventType1, eventType2);
        assertNotSame(eventType1, eventType3);
    }
    
    @Test
    @DisplayName("Deve verificar hash code dos enums")
    void shouldCheckHashCodeOfEnums() {
        // Given
        UserStatusEventType eventType1 = UserStatusEventType.CREATED;
        UserStatusEventType eventType2 = UserStatusEventType.CREATED;
        UserStatusEventType eventType3 = UserStatusEventType.BLOCKED;
        
        // When & Then
        assertEquals(eventType1.hashCode(), eventType2.hashCode());
        assertNotEquals(eventType1.hashCode(), eventType3.hashCode());
    }
    
    @Test
    @DisplayName("Deve verificar toString dos enums")
    void shouldCheckToStringOfEnums() {
        // When & Then
        assertEquals("CREATED", UserStatusEventType.CREATED.toString());
        assertEquals("BLOCKED", UserStatusEventType.BLOCKED.toString());
        assertEquals("UNLOCKED", UserStatusEventType.UNLOCKED.toString());
        assertEquals("DELETED", UserStatusEventType.DELETED.toString());
        assertEquals("EMAIL_VALIDATED", UserStatusEventType.EMAIL_VALIDATED.toString());
        assertEquals("EMAIL_UPDATED", UserStatusEventType.EMAIL_UPDATED.toString());
        assertEquals("EMAIL_CHANGED", UserStatusEventType.EMAIL_CHANGED.toString());
    }
    
    @Test
    @DisplayName("Deve verificar compareTo dos enums")
    void shouldCheckCompareToOfEnums() {
        // When & Then
        assertTrue(UserStatusEventType.CREATED.compareTo(UserStatusEventType.BLOCKED) < 0);
        assertTrue(UserStatusEventType.BLOCKED.compareTo(UserStatusEventType.UNLOCKED) < 0);
        assertTrue(UserStatusEventType.UNLOCKED.compareTo(UserStatusEventType.DELETED) < 0);
        assertTrue(UserStatusEventType.DELETED.compareTo(UserStatusEventType.EMAIL_VALIDATED) < 0);
        assertTrue(UserStatusEventType.EMAIL_VALIDATED.compareTo(UserStatusEventType.EMAIL_UPDATED) < 0);
        assertTrue(UserStatusEventType.EMAIL_UPDATED.compareTo(UserStatusEventType.EMAIL_CHANGED) < 0);
        assertEquals(0, UserStatusEventType.CREATED.compareTo(UserStatusEventType.CREATED));
    }
    
    @Test
    @DisplayName("Deve verificar se todos os valores são únicos")
    void shouldCheckIfAllValuesAreUnique() {
        // Given
        UserStatusEventType[] values = UserStatusEventType.values();
        
        // When & Then
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i], values[j], 
                    "Valores duplicados encontrados: " + values[i] + " e " + values[j]);
            }
        }
    }
    
    private boolean containsValue(UserStatusEventType[] values, UserStatusEventType target) {
        for (UserStatusEventType value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
