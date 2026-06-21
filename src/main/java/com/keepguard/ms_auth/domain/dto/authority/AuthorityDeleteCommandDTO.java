package com.keepguard.ms_auth.domain.dto.authority;

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
public class AuthorityDeleteCommandDTO {

    @NotNull(message = "ID é obrigatório")
    private UUID id;

    private UUID xApplicationUuid;
}

