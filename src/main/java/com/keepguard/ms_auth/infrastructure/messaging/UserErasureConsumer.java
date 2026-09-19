package com.keepguard.ms_auth.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.dto.user.UserHardDeleteCommandDTO;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.service.session.DeviceSessionCommandService;
import com.keepguard.ms_auth.application.service.user.UserCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserErasureConsumer {

    private final UserRepositoryPort userRepository;
    private final DeviceSessionCommandService deviceSessionCommandService;
    private final UserCommandService userCommandService;
    private final ObjectMapper objectMapper;

    @RabbitListener(
        bindings = @QueueBinding(
            value = @Queue(value = "${keepguard.events.user-erasure-auth-queue:ms.auth.user-erasure}", durable = "true"),
            exchange = @Exchange(value = "${keepguard.events.exchange:keepguard-events-exchange}", type = "topic"),
            key = "${keepguard.events.erasure-routing-key:user.erasure.requested}"
        )
    )
    public void handleUserErasure(String message) {
        try {
            log.info("Recebida solicitação de eliminação de titular (Art. 18 LGPD) no ms-auth: {}", message);
            JsonNode json = objectMapper.readTree(message);
            String userIdStr = json.path("userId").asText();
            if (userIdStr != null && !userIdStr.isBlank()) {
                UUID userId = UUID.fromString(userIdStr);
                userRepository.findByIdUserExternal(userId).ifPresent(user -> {
                    if (user.getCodeUser() != null) {
                        deviceSessionCommandService.revokeAllSessions(user.getCodeUser().toString());
                    }
                    userCommandService.hardDelete(new UserHardDeleteCommandDTO(userId.toString(), user.getCompanyId()));
                    log.info("Sessões e credenciais do titular {} expurgadas com sucesso sob Art. 18 LGPD no ms-auth", userId);
                });
            }
        } catch (Exception e) {
            log.error("Erro ao processar eliminação de titular no ms-auth: {}", e.getMessage(), e);
        }
    }
}
