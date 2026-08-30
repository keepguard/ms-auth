package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenView;
import com.keepguard.ms_auth.application.port.in.OAuthClientPort;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthTokenCommandDTO;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthClientUseCaseService implements OAuthClientPort {

    private final OAuthClientCommandService commandService;
    private final OAuthClientQueryService queryService;

    @Override
    public OAuthClientCreateView create(OAuthClientCreateCommandDTO command) {
        return commandService.create(command);
    }

    @Override
    public OAuthClientView findById(UUID companyId, UUID id) {
        return queryService.findById(companyId, id);
    }

    @Override
    public List<OAuthClientView> listByCompany(UUID companyId) {
        return queryService.listByCompany(companyId);
    }

    @Override
    public OAuthClientView block(OAuthClientIdCommandDTO command) {
        return commandService.block(command);
    }

    @Override
    public OAuthClientView unblock(OAuthClientIdCommandDTO command) {
        return commandService.unblock(command);
    }

    @Override
    public void delete(OAuthClientIdCommandDTO command) {
        commandService.delete(command);
    }

    @Override
    @RateLimiter(name = "oauthTokenAttempt", fallbackMethod = "issueTokenRateLimitExceeded")
    public OAuthTokenView issueToken(OAuthTokenCommandDTO command) {
        return commandService.issueToken(command);
    }

    private OAuthTokenView issueTokenRateLimitExceeded(OAuthTokenCommandDTO command, RequestNotPermitted ex) {
        log.warn("RATE LIMIT EXCEDIDO | oauth token | clientId={} | companyId={}",
                command.getClientId(), command.getCompanyId());
        throw new RuntimeException("Muitas tentativas de token. Aguarde 1 minuto antes de tentar novamente.");
    }
}
