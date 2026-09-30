package com.keepguard.ms_auth.application.port.out.cache;

import com.keepguard.ms_auth.domain.entity.session.RefreshToken;

import java.util.Optional;

public interface RefreshTokenCachePort {

    /**
     * Salva um refresh token opaco, indexado pelo hash do valor.
     *
     * @param refreshToken entidade com hash, sid, familyId etc.
     * @param slidingTtlSeconds TTL deslizante (renovado a cada rotação bem-sucedida)
     */
    void save(RefreshToken refreshToken, long slidingTtlSeconds);

    /**
     * Busca o refresh token pelo hash do valor opaco apresentado pelo cliente.
     */
    Optional<RefreshToken> findByHash(String tokenHash);

    /**
     * Marca o token (já rotacionado) como substituído, mantendo-o vivo por um
     * período de carência curto para tolerar corrida entre abas.
     */
    void markReplaced(String oldTokenHash, String newTokenHash, long graceTtlSeconds);

    /**
     * Revoga toda a família de refresh tokens de uma sessão (reuso detectado
     * ou logout/troca de senha) — qualquer token dessa família passa a falhar.
     */
    void revokeFamily(String familyId);

    /**
     * Verifica se a família do token já foi revogada (roubo/reuso detectado).
     */
    boolean isFamilyRevoked(String familyId);

    /**
     * Remove todos os refresh tokens (por família) associados a um sid, usado
     * em logout e revogação de sessão.
     */
    void removeBySid(String sid);

    /**
     * Marca a sessão (sid) como revogada — consultado pelos BFFs (bff-core)
     * para rejeitar access tokens dessa sessão antes mesmo do JWT expirar,
     * sem precisar checar o token exato no cache de login.
     */
    void markSessionRevoked(String sid, long ttlSeconds);

    /**
     * Verifica se a sessão (sid) foi revogada.
     */
    boolean isSessionRevoked(String sid);
}
