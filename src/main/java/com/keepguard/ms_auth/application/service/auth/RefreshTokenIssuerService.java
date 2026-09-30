package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.ms_auth.application.dto.auth.IssuedTokenPairDTO;
import com.keepguard.ms_auth.application.port.out.cache.RefreshTokenCachePort;
import com.keepguard.ms_auth.application.service.exception.InvalidCredentialsException;
import com.keepguard.ms_auth.domain.entity.session.RefreshToken;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.infrastructure.config.security.JwtService;
import com.keepguard.ms_auth.infrastructure.config.security.RefreshTokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Emite, rotaciona e revoga o par (access JWT + refresh token opaco) de uma
 * sessão (sid). Centraliza a lógica para que login, verificação de MFA e
 * refresh não dupliquem a rotação com carência / detecção de reuso.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenIssuerService {

    private final JwtService jwtService;
    private final RefreshTokenCachePort refreshTokenCachePort;
    private final RefreshTokenGenerator refreshTokenGenerator;

    @Value("${security.jwt.refresh-token.sliding-ttl-seconds}")
    private long slidingTtlSeconds;

    @Value("${security.jwt.refresh-token.absolute-ttl-seconds}")
    private long absoluteTtlSeconds;

    @Value("${security.jwt.refresh-token.rotation-grace-seconds}")
    private long rotationGraceSeconds;

    /**
     * Emite o primeiro par de uma sessão nova (login / verificação de MFA).
     */
    public IssuedTokenPairDTO issueInitialPair(User user, List<String> roles, List<String> authorities,
                                                String tenantId, String clientId, String deviceId,
                                                String companyId) {
        String sid = refreshTokenGenerator.newSessionId();
        String familyId = refreshTokenGenerator.newFamilyId();
        return issuePair(user, roles, authorities, tenantId, clientId, deviceId, companyId, sid, familyId);
    }

    /**
     * Rotaciona o refresh token opaco apresentado, gerando um novo par para a
     * mesma sessão/família. Dentro da janela de carência, um token já
     * substituído ainda é aceito (corrida entre abas). Fora da carência a
     * entrada já expirou do Redis e a apresentação cai em TOKEN_REVOKED — o
     * TTL simples não distingue expiração natural de reuso malicioso, então
     * a família não é revogada automaticamente nesse caso; revogação de
     * família é acionada apenas por sinal explícito (logout, troca de senha,
     * teto absoluto de 90 dias atingido).
     */
    public IssuedTokenPairDTO rotate(String presentedRefreshToken, User user, List<String> roles,
                                      List<String> authorities, String tenantId, String clientId,
                                      String companyId) {
        String presentedHash = refreshTokenGenerator.hash(presentedRefreshToken);
        RefreshToken current = refreshTokenCachePort.findByHash(presentedHash)
                .orElseThrow(() -> new InvalidCredentialsException("Refresh token inválido ou expirado", "TOKEN_REVOKED",
                        Map.of()));

        if (refreshTokenCachePort.isFamilyRevoked(current.getFamilyId())) {
            throw new InvalidCredentialsException("Sessão revogada", "TOKEN_REVOKED", Map.of("sid", current.getSid()));
        }

        if (current.getReplacedByHash() != null) {
            // Token apresentado já havia sido rotacionado antes, mas a entrada
            // ainda está viva (dentro da janela de carência) — trata como
            // corrida entre abas, não como roubo: emite um novo par válido
            // para a mesma família em vez de derrubar a sessão.
            log.info("Refresh dentro da janela de carência — nova rotação tolerada | sid={}", current.getSid());
        }

        String absoluteExpiresAt = current.getAbsoluteExpiresAt();
        if (absoluteExpiresAt != null && Instant.parse(absoluteExpiresAt).isBefore(Instant.now())) {
            refreshTokenCachePort.revokeFamily(current.getFamilyId());
            throw new InvalidCredentialsException("Sessão expirada, faça login novamente", "TOKEN_REVOKED",
                    Map.of("sid", current.getSid()));
        }

        IssuedTokenPairDTO issued = issuePair(user, roles, authorities, tenantId, clientId, current.getDeviceId(),
                companyId, current.getSid(), current.getFamilyId(), absoluteExpiresAt);

        String newHash = refreshTokenGenerator.hash(issued.getRefreshToken());
        refreshTokenCachePort.markReplaced(presentedHash, newHash, rotationGraceSeconds);

        return issued;
    }

    /**
     * Reuso detectado fora da janela de carência (token já tinha sido
     * substituído e a entrada antiga expirou) ou revogação explícita
     * (logout, troca de senha, revogação remota): derruba a família inteira.
     */
    public void revokeSession(String sid) {
        refreshTokenCachePort.removeBySid(sid);
        // TTL igual ao teto absoluto: cobre qualquer access token de 15min
        // ainda válido dessa sessão, sem depender de o refresh já ter sido usado.
        refreshTokenCachePort.markSessionRevoked(sid, absoluteTtlSeconds);
    }

    public void revokeFamily(String familyId) {
        refreshTokenCachePort.revokeFamily(familyId);
    }

    private IssuedTokenPairDTO issuePair(User user, List<String> roles, List<String> authorities, String tenantId,
                                          String clientId, String deviceId, String companyId,
                                          String sid, String familyId) {
        String absoluteExpiresAt = Instant.now().plusSeconds(absoluteTtlSeconds).toString();
        return issuePair(user, roles, authorities, tenantId, clientId, deviceId, companyId, sid, familyId, absoluteExpiresAt);
    }

    private IssuedTokenPairDTO issuePair(User user, List<String> roles, List<String> authorities, String tenantId,
                                          String clientId, String deviceId, String companyId,
                                          String sid, String familyId, String absoluteExpiresAt) {
        String accessToken = jwtService.generateToken(user, roles, authorities, tenantId, clientId, deviceId, sid);

        String opaqueRefreshToken = refreshTokenGenerator.generateOpaqueValue();
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(refreshTokenGenerator.hash(opaqueRefreshToken))
                .sid(sid)
                .codeUser(user.getCodeUser().toString())
                .companyId(companyId)
                .clientId(clientId)
                .deviceId(deviceId)
                .familyId(familyId)
                .createdAt(Instant.now().toString())
                .absoluteExpiresAt(absoluteExpiresAt)
                .revoked(false)
                .build();
        refreshTokenCachePort.save(refreshToken, slidingTtlSeconds);

        return IssuedTokenPairDTO.builder()
                .accessToken(accessToken)
                .refreshToken(opaqueRefreshToken)
                .sid(sid)
                .expiresIn(jwtService.getAccessExpiration() / 1000)
                .build();
    }
}
