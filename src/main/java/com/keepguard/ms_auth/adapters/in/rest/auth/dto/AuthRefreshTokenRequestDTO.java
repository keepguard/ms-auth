package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Um dos dois campos precisa vir preenchido — validado em
 * {@link com.keepguard.ms_auth.adapters.in.rest.auth.AuthController}, não via
 * Bean Validation, porque nenhum dos dois é individualmente obrigatório.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRefreshTokenRequestDTO {

    /**
     * JWT legado usado como credencial de rotação (fluxo anterior à Fase 1).
     * O bff-auth não envia mais este campo — mantido só por compatibilidade
     * com clientes antigos que ainda mandam o JWT como refresh.
     */
    private String token;

    /**
     * Refresh token opaco (novo fluxo). É o único campo que o bff-auth manda
     * hoje — lido do cookie HttpOnly, nunca do corpo original da requisição
     * do navegador.
     */
    private String refreshToken;
}
