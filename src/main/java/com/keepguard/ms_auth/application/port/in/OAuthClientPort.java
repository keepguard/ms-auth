package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenView;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthTokenCommandDTO;

import java.util.List;
import java.util.UUID;

public interface OAuthClientPort {

    OAuthClientCreateView create(OAuthClientCreateCommandDTO command);

    OAuthClientView findById(UUID companyId, UUID id);

    List<OAuthClientView> listByCompany(UUID companyId);

    OAuthClientView block(OAuthClientIdCommandDTO command);

    OAuthClientView unblock(OAuthClientIdCommandDTO command);

    void delete(OAuthClientIdCommandDTO command);

    OAuthTokenView issueToken(OAuthTokenCommandDTO command);
}
