package com.keepguard.ms_auth.adapters.in.rest.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aspectj.weaver.loadtime.Agent;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRequestDTO {

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Password é obrigatório")
    private String password;

}
