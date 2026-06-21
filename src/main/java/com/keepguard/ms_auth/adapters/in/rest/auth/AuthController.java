package com.keepguard.ms_auth.adapters.in.rest.auth;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou X-Application inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_login",
        operation = "realizar login"
    )
    public ResponseEntity<AuthLoginResponseDTO> login(
            @Parameter(description = "Credenciais do usuário", required = true)
            @RequestBody @Valid AuthLoginRequestDTO request,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @RequestHeader("User-Agent") String userAgent) {

        log.info("Realizando login para usuário: {}, application={}, userAgent={}", 
            request.getUsername(), xApplication, userAgent);
        
        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        
        var command = mapper.toLoginCommand(request, xApplicationUuid, userAgent);
        var view = authService.login(command);
        var response = mapper.toLoginResponseDTO(view);
        
        log.info("Login successful for user: {} with application: {}", request.getUsername(), xApplicationUuid);
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
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou X-Application inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_register_login",
        operation = "realizar login após registro"
    )
    public ResponseEntity<AuthLoginResponseDTO> registerLogin(
            @Parameter(description = "Dados para login após registro", required = true)
            @RequestBody @Valid AuthRegisterLoginRequestDTO request,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @RequestHeader("User-Agent") String userAgent) {

        log.info("Realizando register login para usuário: {}, application={}, userAgent={}", 
            request.getUsername(), xApplication, userAgent);
        
        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        
        var command = mapper.toRegisterLoginCommand(request, xApplicationUuid, userAgent);
        var view = authService.registerLogin(command);
        var response = mapper.toLoginResponseDTO(view);
        
        log.info("Register login successful for user: {} with application: {}", request.getUsername(), xApplicationUuid);
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
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou X-Application inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_refresh",
        operation = "renovar token"
    )
    public ResponseEntity<AuthRefreshTokenResponseDTO> refreshToken(
            @Parameter(description = "Dados para renovação do token", required = true)
            @RequestBody @Valid AuthRefreshTokenRequestDTO request,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @RequestHeader("User-Agent") String userAgent) {

        log.info("Renovando token JWT - application={}, userAgent={}", xApplication, userAgent);
        
        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        
        var command = mapper.toRefreshTokenCommand(request, xApplicationUuid, userAgent);
        var view = authService.refreshToken(command);
        var response = mapper.toRefreshTokenResponseDTO(view);
        
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Realizar logout",
        description = "Invalida o token JWT do usuário, fazendo logout do sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthLogoutResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido"),
        @ApiResponse(responseCode = "400", description = "X-Application inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_logout",
        operation = "realizar logout"
    )
    public ResponseEntity<AuthLogoutResponseDTO> logout(
            @Parameter(description = "Token JWT a ser invalidado", required = true)
            @RequestHeader("Authorization") String authorization,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication) {

        log.info("Realizando logout - application={}", xApplication);
        
        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        String token = authorization.replace("Bearer ", "");

        var command = mapper.toLogoutCommand(token, xApplicationUuid);
        var view = authService.logout(command);
        var response = mapper.toLogoutResponseDTO(view);
        
        log.info("Logout successful for application: {}", xApplicationUuid);
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
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String application) {
        
        log.info("Validando token JWT - application={}", application);
        
        var xApplicationUuid = ValidationUtils.validateXApplication(application);
        
        var command = mapper.toValidateTokenCommand(validateTokenDTO, xApplicationUuid);
        authService.validateToken(command);
        
        log.info("Token valid for application: {}", xApplicationUuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(
        summary = "Alterar senha",
        description = "Altera a senha do usuário autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, senha atual incorreta ou X-Application inválido"),
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
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication) {

        log.info("Alterando senha para usuário: {}, application={}", request.getCodeUser(), xApplication);
        
        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        var command = mapper.toChangePasswordCommand(request, xApplicationUuid);
        authService.changePassword(command);
        
        log.info("Password changed successfully for user: {} with application: {}", request.getCodeUser(), xApplicationUuid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Resetar senha",
        description = "Reseta a senha do usuário usando um token de reset válido."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Senha resetada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, token inválido ou X-Application inválido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "auth_reset_password",
        operation = "resetar senha"
    )
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "Dados para reset de senha", required = true)
            @RequestBody @Valid AuthResetPasswordRequestDTO request,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication) {

        log.info("Resetando senha para usuário: {}, application={}, resetToken={}", 
            request.getCodeUser(), xApplication, request.getResetToken());
        
        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        var command = mapper.toResetPasswordCommand(request, xApplicationUuid);
        authService.resetPassword(command);
        
        log.info("Password reset successfully for user: {} with application: {}", request.getCodeUser(), xApplicationUuid);
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
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou X-Application inválido"),
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
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication) {

        log.info("Gerando token de reset | codeUser={} | application={} | messageType={} | templateType={}", 
            request.getCodeUser(), xApplication, request.getMessageType(), request.getTemplateType());
        
        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        var command = mapper.toGenerateResetTokenCommand(request, xApplicationUuid);
        var view = authService.generateResetToken(command);
        var response = mapper.toGenerateResetTokenResponseDTO(view);
        
        log.info("Token de reset gerado com sucesso | codeUser={} | application={}", 
            request.getCodeUser(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }
}