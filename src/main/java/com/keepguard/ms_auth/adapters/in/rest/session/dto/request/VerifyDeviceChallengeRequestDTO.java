package com.keepguard.ms_auth.adapters.in.rest.session.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDeviceChallengeRequestDTO {

    @NotBlank(message = "challengeSessionId é obrigatório")
    private String challengeSessionId;

    @NotBlank(message = "code é obrigatório")
    private String code;

    @Builder.Default
    private Boolean trustDevice = true;
}
