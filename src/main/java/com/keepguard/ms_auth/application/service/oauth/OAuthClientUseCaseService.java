package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientRuntimeSecretViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthServiceRoleViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenViewDTO;
import com.keepguard.ms_auth.application.port.in.OAuthClientPort;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientSearchQueryDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientUpdateCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenCommandDTO;
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
    public OAuthClientCreateViewDTO create(OAuthClientCreateCommandDTO command) {
        return commandService.create(command);
    }

    @Override
    public OAuthClientViewDTO update(OAuthClientUpdateCommandDTO command) {
        return commandService.update(command);
    }

    @Override
    public OAuthClientViewDTO findById(UUID companyId, UUID id) {
        return queryService.findById(companyId, id);
    }

    @Override
    public List<OAuthClientViewDTO> listByCompany(UUID companyId) {
        return queryService.listByCompany(companyId);
    }

    @Override
    public PageResultViewDTO<OAuthClientViewDTO> search(OAuthClientSearchQueryDTO query) {
        return queryService.search(query);
    }

    @Override
    public List<OAuthServiceRoleViewDTO> listServiceRoles() {
        return queryService.listServiceRoles();
    }

    @Override
    public OAuthClientViewDTO block(OAuthClientIdCommandDTO command) {
        return commandService.block(command);
    }

    @Override
    public OAuthClientViewDTO unblock(OAuthClientIdCommandDTO command) {
        return commandService.unblock(command);
    }

    @Override
    public void delete(OAuthClientIdCommandDTO command) {
        commandService.delete(command);
    }

    @Override
    public OAuthClientRuntimeSecretViewDTO findRuntimeSecret(UUID companyId, String clientId, String presentedBase) {
        return queryService.findRuntimeSecret(companyId, clientId, presentedBase);
    }

    @Override
    @RateLimiter(name = "oauthTokenAttempt", fallbackMethod = "issueTokenRateLimitExceeded")
    public OAuthTokenViewDTO issueToken(OAuthTokenCommandDTO command) {
        return commandService.issueToken(command);
    }

    private OAuthTokenViewDTO issueTokenRateLimitExceeded(OAuthTokenCommandDTO command, RequestNotPermitted ex) {
        log.warn("RATE LIMIT EXCEDIDO | oauth token | clientId={} | companyId={}",
                command.getClientId(), command.getCompanyId());
        throw new RuntimeException("Muitas tentativas de token. Aguarde 1 minuto antes de tentar novamente.");
    }
}
