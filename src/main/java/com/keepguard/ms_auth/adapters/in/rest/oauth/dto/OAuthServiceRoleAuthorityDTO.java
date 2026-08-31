package com.keepguard.ms_auth.adapters.in.rest.oauth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authority associada a uma service role")
public class OAuthServiceRoleAuthorityDTO {
    private String name;
    private String description;
}
