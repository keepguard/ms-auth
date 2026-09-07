package com.keepguard.ms_auth.application.dto.authority;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorityGetByNameQueryDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String name;
}

