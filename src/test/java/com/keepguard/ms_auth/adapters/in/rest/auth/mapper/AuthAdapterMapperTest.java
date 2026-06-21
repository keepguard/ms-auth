package com.keepguard.ms_auth.adapters.in.rest.auth.mapper;

import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginRequestDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLoginCommandDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthAdapterMapperTest {

    @InjectMocks
    private AuthAdapterMapper mapper;

    @Test
    @DisplayName("toLoginCommand deve normalizar username para lowercase e trim")
    void toLoginCommand_shouldNormalizeUsername() {
        AuthLoginRequestDTO dto = AuthLoginRequestDTO.builder()
                .username("  TesT.User  ")
                .password("pass")
                .build();

        AuthLoginCommandDTO cmd = mapper.toLoginCommand(dto, UUID.randomUUID(), "UA");
        assertNotNull(cmd);
        assertEquals("test.user", cmd.getUsername());
    }
}


