package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import com.keepguard.lib_common.communication.enums.CommunicationTypeEnum;
import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta da geração de token de recuperação de senha")
public class AuthGenerateResetTokenResponseDTO {

    @Schema(description = "Código único do usuário (UUID)", 
            example = "8d582a9d-7951-41d3-8ee6-30b28fd2dcd4")
    private String codeUser;

    @Schema(description = "Tipo da mensagem que será enviada", 
            example = "EMAIL")
    private MessageTypeEnum messageType;

    @Schema(description = "Tipo de comunicação utilizado", 
            example = "EMAIL")
    private CommunicationTypeEnum communicationType;

    @Schema(description = "Tipo do template de mensagem", 
            example = "RECUPERACAO_SENHA")
    private TemplateTypeEnum templateType;

    @Schema(description = "Token gerado para recuperação de senha", 
            example = "123456")
    private String token;

    @Schema(description = "Tempo de expiração do token em segundos", 
            example = "600")
    private Long expiresInSeconds;
}

