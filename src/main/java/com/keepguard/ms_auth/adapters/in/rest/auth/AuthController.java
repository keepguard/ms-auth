package com.keepguard.ms_auth.adapters.in.rest.auth;


import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRefreshTokenResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthChangePasswordRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRefreshTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthResetPasswordRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthGenerateResetTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthGenerateResetTokenResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthValidateTokenRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLogoutResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthRegisterLoginRequestDTO;

import com.keepguard.ms_auth.application.port.in.AuthPort;
import com.keepguard.ms_auth.adapters.in.rest.auth.mapper.AuthAdapterMapper;

import com.keepguard.ms_auth.infrastructure.util.ClientIpResolver;
import com.keepguard.ms_auth.infrastructure.util.ClientLocation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "APIs para autenticação, autorização e gerenciamento de tokens")
public class AuthController {

    private final AuthPort authService;
    private final AuthAdapterMapper mapper;

    @PostMapping("/login")
    @Operation(
        summary = "Realizar login",
        description = "Autentica um usuário com username e senha, retornando um token JWT válido."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthLoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_login",
        operation = "realizar login"
    )
    public ResponseEntity<AuthLoginResponseDTO> login(
            @Parameter(description = "Credenciais do usuário", required = true)
            @RequestBody @Valid AuthLoginRequestDTO request,
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-Client-ID", defaultValue = "keepguard-default-client") String clientId,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Name", required = false) String deviceName,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest httpRequest) {

        log.info("Realizando login para usuário: {}, companyId={}, clientId={}, deviceId={}",
            request.getUsername(), companyId, clientId, deviceId);

        var ipAddress = ClientIpResolver.from(httpRequest);

        var command = mapper.toLoginCommand(request, companyId, clientId, deviceId, deviceName, deviceType, ipAddress, userAgent);
        if (command != null) {
            command.setLocation(ClientLocation.from(httpRequest));
        }
        var view = authService.login(command);
        var response = mapper.toLoginResponseDTO(view);
        
        log.info("Login processado para user: {} status: {}", request.getUsername(), view.status());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register-login")
    @Operation(
        summary = "Login após registro",
        description = "Realiza login após registro usando senha criptografada. Este endpoint é usado internamente pelo BFF após confirmação de registro."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthLoginResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou X-Tenant-Id inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_register_login",
        operation = "realizar login após registro"
    )
    public ResponseEntity<AuthLoginResponseDTO> registerLogin(
            @Parameter(description = "Dados para login após registro", required = true)
            @RequestBody @Valid AuthRegisterLoginRequestDTO request,
            @Parameter(description = "UUID da empresa", required = true)
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-Client-ID", defaultValue = "keepguard-default-client") String clientId) {

        log.info("Realizando register login para usuário: {}, companyId={}, clientId={}", 
            request.getUsername(), companyId, clientId);
        
        
        var command = mapper.toRegisterLoginCommand(request, companyId, clientId);
        var view = authService.registerLogin(command);
        var response = mapper.toLoginResponseDTO(view);
        
        log.info("Register login successful for user: {} with application: {}", request.getUsername(), companyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar token",
        description = "Renova um token JWT válido, retornando um novo token com tempo de expiração atualizado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token renovado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthRefreshTokenResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou X-Tenant-Id inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_refresh",
        operation = "renovar token"
    )
    public ResponseEntity<AuthRefreshTokenResponseDTO> refreshToken(
            @Parameter(description = "Dados para renovação do token", required = true)
            @RequestBody @Valid AuthRefreshTokenRequestDTO request,
            @Parameter(description = "UUID da empresa", required = true)
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-Client-ID", defaultValue = "keepguard-default-client") String clientId) {

        log.info("Renovando token JWT - companyId={}, clientId={}", companyId, clientId);
        
        
        var command = mapper.toRefreshTokenCommand(request, companyId, clientId);
        var view = authService.refreshToken(command);
        var response = mapper.toRefreshTokenResponseDTO(view);
        
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Realizar logout",
        description = "Invalida o token JWT do usuário sem encerrar a sessão do dispositivo. O MFA permanece dispensado neste aparelho no próximo login."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthLogoutResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido"),
        @ApiResponse(responseCode = "400", description = "X-Tenant-Id inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_logout",
        operation = "realizar logout"
    )
    public ResponseEntity<AuthLogoutResponseDTO> logout(
            @Parameter(description = "Token JWT a ser invalidado", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "UUID da empresa", required = true)
            @RequestHeader("X-Company-Id") UUID companyId) {

        log.info("Realizando logout - companyId={}", companyId);
        
        String token = authorization.replace("Bearer ", "");

        var command = mapper.toLogoutCommand(token, companyId);
        var view = authService.logout(command);
        var response = mapper.toLogoutResponseDTO(view);
        
        log.info("Logout successful for application: {}", companyId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(
        summary = "Validar token",
        description = "Valida se um token JWT é válido e não expirou. " +
                    "Retorna 200 se o token for válido, 401 caso contrário."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token válido"),
        @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
        @ApiResponse(responseCode = "400", description = "Token não fornecido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_validate",
        operation = "validar token"
    )
    public ResponseEntity<Void> validate(
            @Parameter(description = "Token JWT a ser validado", required = true)
            @RequestBody @Valid AuthValidateTokenRequestDTO validateTokenDTO, 
            @Parameter(description = "Identificador da empresa", required = true)
            @RequestHeader("X-Company-Id") UUID companyId) {
        
        log.info("Validando token JWT - companyId={}", companyId);
        
        
        var command = mapper.toValidateTokenCommand(validateTokenDTO, companyId);
        authService.validateToken(command);
        
        log.info("Token valid for application: {}", companyId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Alterar senha",
        description = "Altera a senha do usuário autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, senha atual incorreta ou X-Tenant-Id inválido"),
        @ApiResponse(responseCode = "401", description = "Token inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_change_password",
        operation = "alterar senha"
    )
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Dados para alteração de senha", required = true)
            @RequestBody @Valid AuthChangePasswordRequestDTO request,
            @Parameter(description = "UUID da empresa", required = true)
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Name", required = false) String deviceName,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest httpRequest) {

        log.info("Alterando senha para usuário: {}, companyId={}, deviceId={}",
                request.getCodeUser(), companyId, deviceId);
        
        var ipAddress = ClientIpResolver.from(httpRequest);
        var command = mapper.toChangePasswordCommand(
                request, companyId, deviceId, deviceName, deviceType, ipAddress, userAgent);
        authService.changePassword(command);
        
        log.info("Password changed successfully for user: {} with application: {}", request.getCodeUser(), companyId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Resetar senha",
        description = "Reseta a senha do usuário usando um token de reset válido."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha resetada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, token inválido ou X-Tenant-Id inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_reset_password",
        operation = "resetar senha"
    )
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "Dados para reset de senha", required = true)
            @RequestBody @Valid AuthResetPasswordRequestDTO request,
            @Parameter(description = "UUID da empresa", required = true)
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @RequestHeader(value = "X-Device-Name", required = false) String deviceName,
            @RequestHeader(value = "X-Device-Type", required = false) String deviceType,
            @RequestHeader(value = "User-Agent", required = false) String userAgent,
            HttpServletRequest httpRequest) {

        log.info("Resetando senha para usuário: {}, companyId={}, deviceId={}, resetToken={}", 
            request.getCodeUser(), companyId, deviceId, request.getResetToken());
        
        var ipAddress = ClientIpResolver.from(httpRequest);
        var command = mapper.toResetPasswordCommand(
                request, companyId, deviceId, deviceName, deviceType, ipAddress, userAgent);
        authService.resetPassword(command);
        
        log.info("Password reset successfully for user: {} with application: {}", request.getCodeUser(), companyId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate-reset-token")
    @Operation(
        summary = "Gerar token de recuperação de senha",
        description = "Gera um token de 6 dígitos para recuperação de senha e armazena no cache Redis com TTL configurado. " +
                     "O token será utilizado posteriormente para validar a solicitação de reset de senha."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token gerado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthGenerateResetTokenResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou X-Tenant-Id inválido"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_generate_reset_token",
        operation = "gerar token de reset"
    )
    public ResponseEntity<AuthGenerateResetTokenResponseDTO> generateResetToken(
            @Parameter(description = "Dados para geração de token de reset", required = true)
            @RequestBody @Valid AuthGenerateResetTokenRequestDTO request,
            @Parameter(description = "UUID da empresa", required = true)
            @RequestHeader("X-Company-Id") UUID companyId) {

        log.info("Gerando token de reset | codeUser={} | companyId={} | messageType={} | templateType={}", 
            request.getCodeUser(), companyId, request.getMessageType(), request.getTemplateType());
        
        var command = mapper.toGenerateResetTokenCommand(request, companyId);
        var view = authService.generateResetToken(command);
        var response = mapper.toGenerateResetTokenResponseDTO(view);
        
        log.info("Token de reset gerado com sucesso | codeUser={} | application={}", 
            request.getCodeUser(), companyId);
        return ResponseEntity.ok(response);
    }
}