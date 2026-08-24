package com.keepguard.ms_auth.application.dto.session;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendDeviceChallengeCommandDTO {

    @NotBlank(message = "challengeSessionId é obrigatório")
    private String challengeSessionId;

    @NotBlank(message = "channel é obrigatório (EMAIL, SMS, WHATSAPP)")
    private String channel;

    private String tenantId;
}
