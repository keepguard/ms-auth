package com.keepguard.ms_auth.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.dto.user.UserHardDeleteCommandDTO;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.service.session.DeviceSessionCommandService;
import com.keepguard.ms_auth.application.service.user.UserCommandService;
import com.keepguard.ms_auth.domain.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserErasureConsumer Tests")
class UserErasureConsumerTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private DeviceSessionCommandService deviceSessionCommandService;

    @Mock
    private UserCommandService userCommandService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private UserErasureConsumer consumer;

    private UUID userId;
    private UUID codeUser;
    private UUID companyId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        codeUser = UUID.randomUUID();
        companyId = UUID.randomUUID();

        user = mock(User.class);
        when(user.getCodeUser()).thenReturn(codeUser);
        when(user.getCompanyId()).thenReturn(companyId);
    }

    @Test
    @DisplayName("Deve revogar sessões e deletar usuário ao receber user.erasure.requested")
    void shouldRevokeSessionsAndHardDeleteOnErasureRequested() {
        // Given
        when(userRepository.findByIdUserExternal(userId)).thenReturn(Optional.of(user));
        String message = String.format("{\"userId\":\"%s\",\"companyId\":\"%s\"}", userId, companyId);

        // When
        consumer.handleUserErasure(message);

        // Then
        verify(deviceSessionCommandService).revokeAllSessions(codeUser.toString());
        verify(userCommandService).hardDelete(any(UserHardDeleteCommandDTO.class));
    }
}
