package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthServiceRoleViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientRuntimeSecretViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenViewDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientCreateCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientIdCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientSearchQueryDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientUpdateCommandDTO;
import com.keepguard.ms_auth.application.dto.oauth.OAuthTokenCommandDTO;

import java.util.List;
import java.util.UUID;

public interface OAuthClientPort {

    OAuthClientCreateViewDTO create(OAuthClientCreateCommandDTO command);

    OAuthClientViewDTO update(OAuthClientUpdateCommandDTO command);

    OAuthClientViewDTO findById(UUID companyId, UUID id);

    List<OAuthClientViewDTO> listByCompany(UUID companyId);

    PageResultViewDTO<OAuthClientViewDTO> search(OAuthClientSearchQueryDTO query);

    List<OAuthServiceRoleViewDTO> listServiceRoles();

    OAuthClientViewDTO block(OAuthClientIdCommandDTO command);

    OAuthClientViewDTO unblock(OAuthClientIdCommandDTO command);

    void delete(OAuthClientIdCommandDTO command);

    OAuthClientRuntimeSecretViewDTO findRuntimeSecret(UUID companyId, String clientId, String presentedBase);

    OAuthTokenViewDTO issueToken(OAuthTokenCommandDTO command);
}
