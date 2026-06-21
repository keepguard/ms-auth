package com.keepguard.ms_auth.adapters.in.rest.helper;

import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthSimulateResetTokenRequestDTO;
import com.keepguard.ms_auth.application.port.out.cache.TokenCachePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Slf4j
@RestController
@RequestMapping("/api/v1/helper")
@RequiredArgsConstructor
@Profile({"dev", "local", "test"})
@Tag(name = "Utilitários", description = "APIs auxiliares para desenvolvimento e testes (disponível apenas em ambientes de desenvolvimento)")
public class HelperController {

    private final TokenCachePort tokenCachePort;

    @GetMapping("/health")
    @Operation(
        summary = "Health check do Helper",
        description = "Verifica se o serviço Helper está funcionando corretamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Helper funcionando corretamente"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<Map<String, String>> health() {
        log.info("Verificando health do Helper");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Helper API");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }


    @PostMapping("/simulate-reset-token")
    @Operation(
        summary = "Simular token de reset",
        description = "Gera um token de reset simulado para testes. " +
                    "Esta API está disponível apenas em ambientes de desenvolvimento (dev, local, test)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token de reset gerado com sucesso",
                    content = @Content(schema = @Schema(example = "{\"resetToken\": \"abc123def456ghi789\"}"))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<?> simulateResetToken(
            @Parameter(description = "Dados para simulação do token de reset", required = true)
            @RequestBody AuthSimulateResetTokenRequestDTO request) {
        log.info("Simulando token de reset para usuário: {}", request.getCodeUser());
        long ttl = request.getTtlMillis() != null ? request.getTtlMillis() : 15 * 60 * 1000; // 15 min padrão

        // Gerar token automaticamente se não fornecido
        String token = request.getToken();
        if (token == null || token.trim().isEmpty()) {
            token = UUID.randomUUID().toString();
        }

        // Usa valores padrão para messageType e templateType no helper
        tokenCachePort.saveToken(request.getCodeUser(), "EMAIL", "RECUPERACAO_SENHA", token, ttl);
        Map<String, String> response = new HashMap<>();
        response.put("resetToken", token);
        return ResponseEntity.ok(response);
    }

}