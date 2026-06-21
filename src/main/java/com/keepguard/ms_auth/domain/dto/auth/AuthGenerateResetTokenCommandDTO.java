package com.keepguard.ms_auth.domain.dto.auth;

import com.keepguard.lib_common.communication.enums.CommunicationTypeEnum;
import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthGenerateResetTokenCommandDTO {

    @NotBlank(message = "Code User é obrigatório")
    private String codeUser;

    @NotNull(message = "Message Type é obrigatório")
    private MessageTypeEnum messageType;

    @NotNull(message = "Communication Type é obrigatório")
    private CommunicationTypeEnum communicationType;

    @NotNull(message = "Template Type é obrigatório")
    private TemplateTypeEnum templateType;

    @NotNull(message = "X-Application é obrigatório")
    private UUID xApplicationUuid;
}

