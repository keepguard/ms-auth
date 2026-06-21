package com.keepguard.ms_auth.adapters.in.rest.user.mapper;

import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.UserCreateRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.UserSearchRequestDTO;
import com.keepguard.ms_auth.domain.dto.user.UserCreateCommandDTO;
import com.keepguard.ms_auth.domain.dto.user.UserGetByUsernameQueryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserAdapterMapperNormalizationTest {

    @InjectMocks
    private UserAdapterMapper mapper;

    @Test
    @DisplayName("toCreateCommand deve normalizar username para lowercase e trim")
    void toCreateCommand_shouldNormalizeUsername() {
        UserCreateRequestDTO dto = UserCreateRequestDTO.builder()
                .username("  JoAo.Silva  ")
                .email("test@example.com")
                .password("password123")
                .idUserExternal("EXT123")
                .codeUser("USR123")
                .companyId(UUID.randomUUID().toString())
                .companyCode(UUID.randomUUID().toString())
                .xApplication(UUID.randomUUID().toString())
                .build();

        UserCreateCommandDTO cmd = mapper.toCreateCommand(dto, UUID.randomUUID());
        assertNotNull(cmd);
        assertEquals("joao.silva", cmd.getUsername());
    }

    @Test
    @DisplayName("toGetByUsernameQuery deve normalizar username para lowercase e trim")
    void toGetByUsernameQuery_shouldNormalizeUsername() {
        UserGetByUsernameQueryDTO q = mapper.toGetByUsernameQuery("  ABC.DEF  ", UUID.randomUUID());
        assertNotNull(q);
        assertEquals("abc.def", q.getUsername());
    }

    @Test
    @DisplayName("toSearchQuery deve normalizar username para lowercase e trim")
    void toSearchQuery_shouldNormalizeUsername() {
        UserSearchRequestDTO req = UserSearchRequestDTO.builder()
                .username("  TEST.USER  ")
                .build();
        var q = mapper.toSearchQuery(req, UUID.randomUUID());
        assertNotNull(q);
        assertEquals("test.user", q.getUsername());
    }
}
