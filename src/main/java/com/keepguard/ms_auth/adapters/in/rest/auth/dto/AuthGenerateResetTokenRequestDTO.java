package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import com.keepguard.lib_common.communication.enums.CommunicationTypeEnum;
import com.keepguard.lib_common.communication.enums.MessageTypeEnum;
import com.keepguard.lib_common.communication.enums.TemplateTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para geração de token de recuperação de senha")
public class AuthGenerateResetTokenRequestDTO {

    @NotBlank(message = "Code User é obrigatório")
    @Schema(description = "Código único do usuário (UUID)", 
            example = "8d582a9d-7951-41d3-8ee6-30b28fd2dcd4", 
            required = true)
    private String codeUser;

    @NotNull(message = "Message Type é obrigatório")
    @Schema(description = "Tipo da mensagem que será enviada", 
            example = "EMAIL", 
            required = true)
    private MessageTypeEnum messageType;

    @NotNull(message = "Communication Type é obrigatório")
    @Schema(description = "Tipo de comunicação utilizado", 
            example = "EMAIL", 
            required = true)
    private CommunicationTypeEnum communicationType;

    @NotNull(message = "Template Type é obrigatório")
    @Schema(description = "Tipo do template de mensagem", 
            example = "RECUPERACAO_SENHA", 
            required = true)
    private TemplateTypeEnum templateType;
}

