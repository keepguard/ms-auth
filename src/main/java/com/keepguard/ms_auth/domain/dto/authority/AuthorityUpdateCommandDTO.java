package com.keepguard.ms_auth.domain.dto.authority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityUpdateCommandDTO {

    @NotNull(message = "ID é obrigatório")
    private UUID id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
    private String name;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String description;

    private UUID xApplicationUuid;
}

