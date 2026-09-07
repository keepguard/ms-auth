package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.auth.AuthLoginCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthValidateTokenQueryDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthChangePasswordCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthResetPasswordCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthGenerateResetTokenCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthGenerateResetTokenViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRegisterLoginCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutViewDTO;
import com.keepguard.ms_auth.application.dto.user.UserViewDTO;

import java.util.Optional;
import java.util.UUID;

public interface AuthPort {

    AuthLoginViewDTO login(AuthLoginCommandDTO request);

    AuthLoginViewDTO registerLogin(AuthRegisterLoginCommandDTO request);

    AuthRefreshTokenViewDTO refreshToken(AuthRefreshTokenCommandDTO request);

    AuthLogoutViewDTO logout(AuthLogoutCommandDTO request);

    void validateToken(AuthValidateTokenQueryDTO request);

    void changePassword(AuthChangePasswordCommandDTO request);

    void resetPassword(AuthResetPasswordCommandDTO request);

    AuthGenerateResetTokenViewDTO generateResetToken(AuthGenerateResetTokenCommandDTO request);

    Optional<UserViewDTO> findByUsername(String username, UUID companyId);

    Optional<UserViewDTO> findByEmail(String email, UUID companyId);

    Optional<UserViewDTO> findByIdUserExternal(UUID idUserExternal);

    Optional<UserViewDTO> findByCodeUser(UUID codeUser);
}
