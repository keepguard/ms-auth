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
public class SendDeviceChallengeRequestDTO {

    @NotBlank(message = "challengeSessionId é obrigatório")
    private String challengeSessionId;

    @NotBlank(message = "channel é obrigatório (EMAIL, SMS, WHATSAPP)")
    private String channel;
}
