package com.keepguard.ms_auth.domain.dto.user;

import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.UserCreateRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserCreateValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("UserCreateRequestDTO deve invalidar username curto, caracteres inválidos e palavras proibidas")
    void requestDTO_shouldInvalidateUsername() {
        UserCreateRequestDTO dto = UserCreateRequestDTO.builder()
                .username("ab") // curto
                .email("test@example.com")
                .password("password123")
                .idUserExternal("EXT")
                .codeUser("USR")
                .companyId(UUID.randomUUID().toString())
                .companyCode(UUID.randomUUID().toString())
                .xApplication(UUID.randomUUID().toString())
                .build();
        Set<ConstraintViolation<UserCreateRequestDTO>> v1 = validator.validate(dto);
        assertFalse(v1.isEmpty());

        dto = UserCreateRequestDTO.builder()
                .username("invalid-$")
                .email("test@example.com")
                .password("password123")
                .idUserExternal("EXT")
                .codeUser("USR")
                .companyId(UUID.randomUUID().toString())
                .companyCode(UUID.randomUUID().toString())
                .xApplication(UUID.randomUUID().toString())
                .build();
        Set<ConstraintViolation<UserCreateRequestDTO>> v2 = validator.validate(dto);
        assertFalse(v2.isEmpty());

        dto = UserCreateRequestDTO.builder()
                .username("user_test") // contém 'user'
                .email("test@example.com")
                .password("password123")
                .idUserExternal("EXT")
                .codeUser("USR")
                .companyId(UUID.randomUUID().toString())
                .companyCode(UUID.randomUUID().toString())
                .xApplication(UUID.randomUUID().toString())
                .build();
        Set<ConstraintViolation<UserCreateRequestDTO>> v3 = validator.validate(dto);
        assertFalse(v3.isEmpty());

        dto = UserCreateRequestDTO.builder()
                .username("abc.def_123")
                .email("test@example.com")
                .password("password123")
                .idUserExternal("EXT")
                .codeUser("USR")
                .companyId(UUID.randomUUID().toString())
                .companyCode(UUID.randomUUID().toString())
                .xApplication(UUID.randomUUID().toString())
                .build();
        Set<ConstraintViolation<UserCreateRequestDTO>> v4 = validator.validate(dto);
        assertTrue(v4.isEmpty());
    }
}
