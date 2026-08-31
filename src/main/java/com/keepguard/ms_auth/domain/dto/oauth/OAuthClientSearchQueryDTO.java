package com.keepguard.ms_auth.domain.dto.oauth;

import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthClientSearchQueryDTO {
    private UUID companyId;
    private String clientId;
    private OAuthClientStatus status;
    private Pageable pageable;
}
