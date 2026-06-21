package com.keepguard.ms_auth.domain.dto.auth;

import com.keepguard.lib_common.communication.enums.CommunicationTypeEnum;
import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthGenerateResetTokenViewDTO {

    private String codeUser;
    private MessageTypeEnum messageType;
    private CommunicationTypeEnum communicationType;
    private TemplateTypeEnum templateType;
    private String token;
    private Long expiresInSeconds;
}

