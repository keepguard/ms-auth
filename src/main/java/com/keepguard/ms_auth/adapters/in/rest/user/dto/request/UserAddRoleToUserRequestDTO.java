package com.keepguard.ms_auth.adapters.in.rest.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddRoleToUserRequestDTO {
    @NotBlank(message = "Nome da role é obrigatório")
    private String role;
}

