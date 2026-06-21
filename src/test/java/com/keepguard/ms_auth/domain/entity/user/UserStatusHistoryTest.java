package com.keepguard.ms_auth.domain.entity.user;

import com.keepguard.ms_auth.domain.enums.UserStatusEventType;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade UserStatusHistory
 */
class UserStatusHistoryTest {
    
    private UserStatusHistory userStatusHistory;
    private User user;
    private UUID historyId;
    
    @BeforeEach
    void setUp() {
        historyId = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .asActive()
            .buildDomain();
        
        userStatusHistory = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.CREATED)
            .reason("Usuário criado no sistema")
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    @Test
    @DisplayName("Deve criar histórico de status com dados válidos")
    void shouldCreateUserStatusHistoryWithValidData() {
        // Then
        assertNotNull(userStatusHistory.getId());
        assertEquals(user.getId(), userStatusHistory.getUserId());
        assertEquals(UserStatusEventType.CREATED, userStatusHistory.getEventType());
        assertEquals("Usuário criado no sistema", userStatusHistory.getReason());
        assertNotNull(userStatusHistory.getCreatedAt());
    }
    
    @Test
    @DisplayName("Deve criar histórico com ID específico")
    void shouldCreateUserStatusHistoryWithSpecificId() {
        // Given
        UUID specificId = UUID.randomUUID();
        
        // When
        UserStatusHistory historyWithId = UserStatusHistory.builder()
            .id(specificId)
            .userId(user.getId())
            .eventType(UserStatusEventType.BLOCKED)
            .reason("Usuário bloqueado por violação de política")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(specificId, historyWithId.getId());
    }
    
    @Test
    @DisplayName("Deve criar histórico para evento de bloqueio")
    void shouldCreateUserStatusHistoryForBlockedEvent() {
        // When
        UserStatusHistory blockedHistory = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.BLOCKED)
            .reason("Usuário bloqueado por tentativas de login suspeitas")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(UserStatusEventType.BLOCKED, blockedHistory.getEventType());
        assertEquals("Usuário bloqueado por tentativas de login suspeitas", blockedHistory.getReason());
    }
    
    @Test
    @DisplayName("Deve criar histórico para evento de desbloqueio")
    void shouldCreateUserStatusHistoryForUnlockedEvent() {
        // When
        UserStatusHistory unlockedHistory = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.UNLOCKED)
            .reason("Usuário desbloqueado após verificação de identidade")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(UserStatusEventType.UNLOCKED, unlockedHistory.getEventType());
        assertEquals("Usuário desbloqueado após verificação de identidade", unlockedHistory.getReason());
    }
    
    @Test
    @DisplayName("Deve criar histórico para evento de deleção")
    void shouldCreateUserStatusHistoryForDeletedEvent() {
        // When
        UserStatusHistory deletedHistory = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.DELETED)
            .reason("Usuário deletado por solicitação do administrador")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(UserStatusEventType.DELETED, deletedHistory.getEventType());
        assertEquals("Usuário deletado por solicitação do administrador", deletedHistory.getReason());
    }
    
    @Test
    @DisplayName("Deve criar histórico para evento de validação de email")
    void shouldCreateUserStatusHistoryForEmailValidatedEvent() {
        // When
        UserStatusHistory emailValidatedHistory = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.EMAIL_VALIDATED)
            .reason("Email validado com sucesso")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(UserStatusEventType.EMAIL_VALIDATED, emailValidatedHistory.getEventType());
        assertEquals("Email validado com sucesso", emailValidatedHistory.getReason());
    }
    
    @Test
    @DisplayName("Deve criar histórico para evento de atualização de email")
    void shouldCreateUserStatusHistoryForEmailUpdatedEvent() {
        // When
        UserStatusHistory emailUpdatedHistory = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.EMAIL_UPDATED)
            .reason("Email atualizado pelo usuário")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(UserStatusEventType.EMAIL_UPDATED, emailUpdatedHistory.getEventType());
        assertEquals("Email atualizado pelo usuário", emailUpdatedHistory.getReason());
    }
    
    @Test
    @DisplayName("Deve criar histórico para evento de mudança de email")
    void shouldCreateUserStatusHistoryForEmailChangedEvent() {
        // When
        UserStatusHistory emailChangedHistory = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.EMAIL_CHANGED)
            .reason("Email alterado pelo administrador")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(UserStatusEventType.EMAIL_CHANGED, emailChangedHistory.getEventType());
        assertEquals("Email alterado pelo administrador", emailChangedHistory.getReason());
    }
    
    @Test
    @DisplayName("Deve criar histórico com data específica")
    void shouldCreateUserStatusHistoryWithSpecificDate() {
        // Given
        LocalDateTime specificDate = LocalDateTime.now().minusDays(30);
        
        // When
        UserStatusHistory historyWithDate = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.CREATED)
            .reason("Usuário criado no sistema")
            .createdAt(specificDate)
            .build();
        
        // Then
        assertEquals(specificDate, historyWithDate.getCreatedAt());
    }
    
    @Test
    @DisplayName("Deve criar histórico com usuário específico")
    void shouldCreateUserStatusHistoryWithSpecificUser() {
        // Given
        User specificUser = UserTestBuilder.aUser()
            .withUsername("specificuser")
            .withEmail("specific@example.com")
            .buildDomain();
        
        // When
        UserStatusHistory historyWithUser = UserStatusHistory.builder()
            .id(historyId)
            .userId(specificUser.getId())
            .eventType(UserStatusEventType.CREATED)
            .reason("Usuário específico criado")
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(specificUser.getId(), historyWithUser.getUserId());
    }
    
    @Test
    @DisplayName("Deve criar histórico com razão longa")
    void shouldCreateUserStatusHistoryWithLongReason() {
        // Given
        String longReason = "Usuário bloqueado por múltiplas violações de política de segurança, incluindo tentativas de acesso não autorizado e uso inadequado do sistema";
        
        // When
        UserStatusHistory historyWithLongReason = UserStatusHistory.builder()
            .id(historyId)
            .userId(user.getId())
            .eventType(UserStatusEventType.BLOCKED)
            .reason(longReason)
            .createdAt(LocalDateTime.now())
            .build();
        
        // Then
        assertEquals(longReason, historyWithLongReason.getReason());
    }
    
    @Test
    @DisplayName("Deve criar histórico com todos os tipos de evento")
    void shouldCreateUserStatusHistoryWithAllEventTypes() {
        // Given
        UserStatusEventType[] eventTypes = UserStatusEventType.values();
        
        // When & Then
        for (UserStatusEventType eventType : eventTypes) {
            UserStatusHistory history = UserStatusHistory.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .eventType(eventType)
                .reason("Teste para evento: " + eventType.name())
                .createdAt(LocalDateTime.now())
                .build();
            
            assertEquals(eventType, history.getEventType());
            assertNotNull(history.getId());
            assertEquals(user.getId(), history.getUserId());
            assertNotNull(history.getCreatedAt());
        }
    }
}
