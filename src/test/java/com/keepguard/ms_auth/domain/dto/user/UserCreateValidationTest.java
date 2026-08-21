package com.keepguard.ms_auth.domain.dto.user;

import com.keepguard.ms_auth.domain.dto.user.UserCreateCommandDTO;
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
    @DisplayName("UserCreateCommandDTO deve invalidar username curto, caracteres inválidos e palavras proibidas")
    void requestDTO_shouldInvalidateUsername() {
        UserCreateCommandDTO dto = UserCreateCommandDTO.builder()
                .username("ab") // curto
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .idUserExternal("EXT")
                .codeUser(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .companyCode(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .build();
        Set<ConstraintViolation<UserCreateCommandDTO>> v1 = validator.validate(dto);
        assertFalse(v1.isEmpty());

        dto = UserCreateCommandDTO.builder()
                .username("invalid-$")
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .idUserExternal("EXT")
                .codeUser(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .companyCode(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .build();
        Set<ConstraintViolation<UserCreateCommandDTO>> v2 = validator.validate(dto);
        assertFalse(v2.isEmpty());

        dto = UserCreateCommandDTO.builder()
                .username("user_test") // contém 'user'
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .idUserExternal("EXT")
                .codeUser(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .companyCode(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .build();
        Set<ConstraintViolation<UserCreateCommandDTO>> v3 = validator.validate(dto);
        assertFalse(v3.isEmpty());

        dto = UserCreateCommandDTO.builder()
                .username("abc.def_123")
                .email("test@example.com")
                .password("password123")
                .name("Test User")
                .idUserExternal("EXT")
                .codeUser(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .companyCode(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .build();
        Set<ConstraintViolation<UserCreateCommandDTO>> v4 = validator.validate(dto);
        assertTrue(v4.isEmpty());
    }
}
