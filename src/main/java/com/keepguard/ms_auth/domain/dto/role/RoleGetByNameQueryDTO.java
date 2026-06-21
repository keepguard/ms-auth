package com.keepguard.ms_auth.domain.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleGetByNameQueryDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String name;
    private UUID xApplicationUuid;
}

