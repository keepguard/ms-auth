package com.keepguard.ms_auth.application.port.out.cache;

/**
 * Port para cache de tokens (login e reset de senha).
 * 
 * <p>Define operações de cache para tokens JWT e tokens de reset de senha
 * utilizando Redis como mecanismo de armazenamento.
 * 
 * @author KeepGuard Team
 * @version 1.0.78
 * @since 1.0.78
 */
public interface TokenCachePort {

    /**
     * Salva um token de login no cache.
     * 
     * @param codeUser Código do usuário
     * @param token Token JWT
     * @param ttlMillis Tempo de vida em milissegundos
     */
    void saveToken(String codeUser, String token, long ttlMillis);

    /**
     * Verifica se um token de login é válido.
     * 
     * @param codeUser Código do usuário
     * @param token Token JWT
     * @return true se o token é válido, false caso contrário
     */
    boolean isTokenValid(String codeUser, String token);

    /**
     * Remove todos os tokens de login de um usuário.
     * 
     * @param codeUser Código do usuário
     */
    void removeAllTokens(String codeUser);

    /**
     * Remove um token específico de login.
     * 
     * @param codeUser Código do usuário
     * @param token Token JWT
     */
    void removeToken(String codeUser, String token);

    /**
     * Salva um token de reset de senha no cache.
     * 
     * @param codeUser Código do usuário
     * @param messageType Tipo da mensagem
     * @param templateType Tipo do template
     * @param token Token de reset
     * @param ttlMillis Tempo de vida em milissegundos
     */
    void saveToken(String codeUser, String messageType, String templateType, String token, long ttlMillis);

    /**
     * Verifica se um token de reset de senha é válido.
     * 
     * @param codeUser Código do usuário
     * @param messageType Tipo da mensagem
     * @param templateType Tipo do template
     * @param token Token de reset
     * @return true se o token é válido, false caso contrário
     */
    boolean isResetTokenValid(String codeUser, String messageType, String templateType, String token);

    /**
     * Remove um token de reset de senha.
     * 
     * @param codeUser Código do usuário
     * @param messageType Tipo da mensagem
     * @param templateType Tipo do template
     */
    void removeResetToken(String codeUser, String messageType, String templateType);

    /**
     * Gera e salva um novo token de reset de senha no cache.
     * 
     * @param codeUser Código do usuário
     * @param messageType Tipo da mensagem
     * @param templateType Tipo do template
     * @return Token de reset gerado
     */
    String generateAndSaveResetToken(String codeUser, String messageType, String templateType);

}
