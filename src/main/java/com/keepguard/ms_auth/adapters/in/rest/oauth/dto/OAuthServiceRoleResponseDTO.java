package com.keepguard.ms_auth.adapters.in.rest.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Service role global assignável a OAuth clients")
public class OAuthServiceRoleResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private List<OAuthServiceRoleAuthorityDTO> authorities;
}
