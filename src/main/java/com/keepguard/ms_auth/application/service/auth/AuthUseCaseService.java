package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.ms_auth.domain.dto.auth.AuthLoginCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthRefreshTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLogoutCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthValidateTokenQueryDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthChangePasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthResetPasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRegisterLoginCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenView;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutView;
import com.keepguard.ms_auth.application.port.in.AuthPort;
import com.keepguard.ms_auth.application.dto.user.UserView;
import com.keepguard.ms_auth.application.mapper.AuthApplicationMapper;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Serviço de casos de uso de autenticação com resiliência.
 * 
 * <p>Este serviço implementa Rate Limiting para proteger contra:
 * <ul>
 *   <li><b>Ataques de força bruta:</b> Limita tentativas de login</li>
 *   <li><b>Sobrecarga de recursos:</b> Protege recursos do servidor</li>
 * </ul>
 * 
 * <p><b>Rate Limiting configurado:</b>
 * <ul>
 *   <li><b>Login:</b> Máximo 5 tentativas por minuto</li>
 *   <li><b>Refresh Token:</b> Sem limite (usuário já autenticado)</li>
 * </ul>
 * 
 * <p>Quando o limite é excedido, o método de fallback retorna uma
 * mensagem amigável instruindo o usuário a aguardar.
 * 
 * @author KeepGuard Team
 * @version 1.0.77
 * @since 1.0.77
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUseCaseService implements AuthPort {

    private final AuthCommandService authCommandService;
    private final AuthQueryService authQueryService;
    private final AuthApplicationMapper authApplicationMapper;

    /**
     * Processa login com Rate Limiting para prevenir ataques de força bruta.
     * 
     * <p><b>Rate Limit:</b> 5 tentativas por minuto por usuário.
     * 
     * <p>Quando o limite é excedido:
     * <ul>
     *   <li>O fallback {@link #loginRateLimitExceeded} é executado</li>
     *   <li>Uma exceção amigável é lançada ao usuário</li>
     *   <li>O evento é logado para auditoria</li>
     * </ul>
     * 
     * @param request Dados de login (username, password, etc)
     * @return AuthLoginView com token JWT e tempo de expiração
     * @throws RuntimeException quando rate limit é excedido
     */
    @Override
    @RateLimiter(name = "loginAttempt", fallbackMethod = "loginRateLimitExceeded")
    public AuthLoginView login(AuthLoginCommandDTO request) {
        log.info("Processando login | username={} | application={} | userAgent={}", 
            request.getUsername(), request.getXApplicationUuid(), request.getUserAgent());
        
        String token = authCommandService.login(request);
        
        log.info("Login bem-sucedido | username={}", request.getUsername());
        return new AuthLoginView(token, 3600L);
    }

    @Override
    public AuthLoginView registerLogin(AuthRegisterLoginCommandDTO request) {
        log.info("Processando register login | username={} | application={} | userAgent={}", 
            request.getUsername(), request.getXApplicationUuid(), request.getUserAgent());
        
        String token = authCommandService.registerLogin(request);
        
        log.info("Register login bem-sucedido | username={}", request.getUsername());
        return new AuthLoginView(token, 3600L);
    }
    
    /**
     * Fallback executado quando o rate limit de login é excedido.
     * 
     * <p><b>Proteção contra força bruta:</b><br>
     * Este método é ativado quando um usuário (ou atacante) tenta fazer login
     * mais de 5 vezes em 1 minuto. Isso previne ataques de força bruta e
     * protege os recursos do servidor.
     * 
     * <p><b>Resposta:</b><br>
     * Lança uma exceção com mensagem amigável instruindo o usuário a aguardar.
     * 
     * @param request Dados de login original
     * @param ex RequestNotPermitted lançada pelo Rate Limiter
     * @throws RuntimeException com mensagem amigável
     */
    private AuthLoginView loginRateLimitExceeded(AuthLoginCommandDTO request, RequestNotPermitted ex) {
        log.warn("RATE LIMIT EXCEDIDO | username={} | application={} | userAgent={}", 
            request.getUsername(), 
            request.getXApplicationUuid(), 
            request.getUserAgent());
        
        throw new RuntimeException(
            "Muitas tentativas de login. Por favor, aguarde 1 minuto antes de tentar novamente."
        );
    }

    @Override
    public AuthRefreshTokenView refreshToken(AuthRefreshTokenCommandDTO request) {
        log.info("Processing refresh token request - application={}, userAgent={}", 
            request.getXApplicationUuid(), request.getUserAgent());
        String token = authCommandService.refreshToken(request);
        return new AuthRefreshTokenView(token, 3600L);
    }

    @Override
    public AuthLogoutView logout(AuthLogoutCommandDTO request) {
        log.info("Processing logout request - application={}", request.getXApplicationUuid());
        authCommandService.logout(request);
        return new AuthLogoutView("Logout realizado com sucesso", true);
    }

    @Override
    public void validateToken(AuthValidateTokenQueryDTO request) {
        log.info("Processing token validation request - application={}", request.getXApplicationUuid());
        authCommandService.validateToken(request);
    }

    @Override
    public void resetPassword(AuthResetPasswordCommandDTO request) {
        log.info("Processing password reset request - codeUser={}, application={}", 
            request.getCodeUser(), request.getXApplicationUuid());
        authCommandService.resetPassword(request);
    }

    @Override
    public void changePassword(AuthChangePasswordCommandDTO request) {
        log.info("Processing password change request - codeUser={}, application={}", 
            request.getCodeUser(), request.getXApplicationUuid());
        authCommandService.changePassword(request);
    }

    @Override
    public AuthGenerateResetTokenViewDTO generateResetToken(AuthGenerateResetTokenCommandDTO request) {
        log.info("Gerando token de reset | codeUser={} | messageType={} | templateType={}", 
            request.getCodeUser(), request.getMessageType(), request.getTemplateType());
        return authCommandService.generateResetToken(request);
    }

    @Override
    public Optional<UserView> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
            return authQueryService.findByUsername(username)
                    .map(authApplicationMapper::toUserView);
    }

    @Override
    public Optional<UserView> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
            return authQueryService.findByEmail(email)
                    .map(authApplicationMapper::toUserView);
    }

    @Override
    public Optional<UserView> findByIdUserExternal(UUID idUserExternal) {
        log.debug("Finding user by external ID: {}", idUserExternal);
            return authQueryService.findByIdUserExternal(idUserExternal)
                    .map(authApplicationMapper::toUserView);
    }

    @Override
    public Optional<UserView> findByCodeUser(UUID codeUser) {
        log.debug("Finding user by code: {}", codeUser);
            return authQueryService.findByCodeUser(codeUser)
                    .map(authApplicationMapper::toUserView);
    }
}
