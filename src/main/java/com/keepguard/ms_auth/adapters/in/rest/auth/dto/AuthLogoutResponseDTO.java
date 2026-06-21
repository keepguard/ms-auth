package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLogoutResponseDTO {

    private String message;
    private boolean success;
}
