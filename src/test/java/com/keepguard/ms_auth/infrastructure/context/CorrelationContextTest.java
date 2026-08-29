package com.keepguard.ms_auth.infrastructure.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para CorrelationContext
 */
class CorrelationContextTest {
    
    private CorrelationContext correlationContext;
    
    @BeforeEach
    void setUp() {
        correlationContext = new CorrelationContext();
        // Limpar MDC antes de cada teste
        MDC.clear();
    }
    
    @AfterEach
    void tearDown() {
        // Limpar MDC após cada teste
        MDC.clear();
    }
    
    @Test
    @DisplayName("Deve gerar correlation ID quando não existe")
    void shouldGenerateCorrelationIdWhenNotExists() {
        // When
        String correlationId = correlationContext.getCorrelationId();
        
        // Then
        assertNotNull(correlationId);
        assertEquals(36, correlationId.length());
        assertEquals(correlationId, MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
    }
    
    @Test
    @DisplayName("Deve retornar correlation ID existente")
    void shouldReturnExistingCorrelationId() {
        // Given
        String existingCorrelationId = "existing-id";
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, existingCorrelationId);
        
        // When
        String correlationId = correlationContext.getCorrelationId();
        
        // Then
        assertEquals(existingCorrelationId, correlationId);
        assertEquals(existingCorrelationId, MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
    }
    
    @Test
    @DisplayName("Deve definir correlation ID válido")
    void shouldSetValidCorrelationId() {
        // Given
        String newCorrelationId = "new-id";
        
        // When
        correlationContext.setCorrelationId(newCorrelationId);
        
        // Then
        assertEquals(newCorrelationId, MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
        assertEquals(newCorrelationId, correlationContext.getCorrelationId());
    }
    
    @Test
    @DisplayName("Deve gerar novo correlation ID quando definido como nulo")
    void shouldGenerateNewCorrelationIdWhenSetToNull() {
        // Given
        String originalId = "original-id";
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, originalId);
        
        // When
        correlationContext.setCorrelationId(null);
        
        // Then
        String newId = MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY);
        assertNotNull(newId);
        assertEquals(36, newId.length());
        assertNotEquals(originalId, newId);
    }
    
    @Test
    @DisplayName("Deve gerar novo correlation ID quando definido como string vazia")
    void shouldGenerateNewCorrelationIdWhenSetToEmptyString() {
        // Given
        String originalId = "original-id";
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, originalId);
        
        // When
        correlationContext.setCorrelationId("");
        
        // Then
        String newId = MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY);
        assertNotNull(newId);
        assertEquals(36, newId.length());
        assertNotEquals(originalId, newId);
    }
    
    @Test
    @DisplayName("Deve gerar novo correlation ID quando definido como string com espaços")
    void shouldGenerateNewCorrelationIdWhenSetToWhitespaceString() {
        // Given
        String originalId = "original-id";
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, originalId);
        
        // When
        correlationContext.setCorrelationId("   ");
        
        // Then
        String newId = MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY);
        assertNotNull(newId);
        assertEquals(36, newId.length());
        assertNotEquals(originalId, newId);
    }
    
    @Test
    @DisplayName("Deve limpar correlation ID")
    void shouldClearCorrelationId() {
        // Given
        String correlationId = "test-id";
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, correlationId);
        assertEquals(correlationId, MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
        
        // When
        correlationContext.clearCorrelationId();
        
        // Then
        assertNull(MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
    }
    
    @Test
    @DisplayName("Deve gerar correlation ID único a cada chamada")
    void shouldGenerateUniqueCorrelationIdOnEachCall() {
        // When
        String id1 = correlationContext.getCorrelationId();
        MDC.clear();
        String id2 = correlationContext.getCorrelationId();
        MDC.clear();
        String id3 = correlationContext.getCorrelationId();
        
        // Then
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
        
        // Todos devem começar com o prefixo correto
        assertEquals(36, id1.length());
        assertEquals(36, id2.length());
        assertEquals(36, id3.length());
    }
    
    @Test
    @DisplayName("Deve manter correlation ID consistente entre chamadas")
    void shouldMaintainConsistentCorrelationIdBetweenCalls() {
        // When
        String id1 = correlationContext.getCorrelationId();
        String id2 = correlationContext.getCorrelationId();
        String id3 = correlationContext.getCorrelationId();
        
        // Then
        assertEquals(id1, id2);
        assertEquals(id2, id3);
        assertEquals(id1, id3);
    }
    
    @Test
    @DisplayName("Deve verificar constantes de header e MDC")
    void shouldVerifyHeaderAndMdcConstants() {
        // Then
        assertEquals("X-Correlation-ID", CorrelationContext.CORRELATION_ID_HEADER);
        assertEquals("correlationId", CorrelationContext.CORRELATION_ID_MDC_KEY);
    }
    
    @Test
    @DisplayName("Deve funcionar com correlation ID personalizado")
    void shouldWorkWithCustomCorrelationId() {
        // Given
        String customId = "custom-correlation-id-123";
        
        // When
        correlationContext.setCorrelationId(customId);
        String retrievedId = correlationContext.getCorrelationId();
        
        // Then
        assertEquals(customId, retrievedId);
        assertEquals(customId, MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
    }
    
    @Test
    @DisplayName("Deve gerar correlation ID após limpeza")
    void shouldGenerateCorrelationIdAfterClear() {
        // Given
        String originalId = "original-id";
        MDC.put(CorrelationContext.CORRELATION_ID_MDC_KEY, originalId);
        correlationContext.clearCorrelationId();
        assertNull(MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
        
        // When
        String newId = correlationContext.getCorrelationId();
        
        // Then
        assertNotNull(newId);
        assertEquals(36, newId.length());
        assertNotEquals(originalId, newId);
        assertEquals(newId, MDC.get(CorrelationContext.CORRELATION_ID_MDC_KEY));
    }
}
