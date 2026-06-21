package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.domain.dto.auth.AuthLoginCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthRefreshTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthLogoutCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthValidateTokenQueryDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthChangePasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthResetPasswordCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenCommandDTO;
import com.keepguard.ms_auth.domain.dto.auth.AuthGenerateResetTokenViewDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthRegisterLoginCommandDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginView;
import com.keepguard.ms_auth.application.dto.auth.AuthRefreshTokenView;
import com.keepguard.ms_auth.application.dto.auth.AuthLogoutView;
import com.keepguard.ms_auth.application.dto.user.UserView;

import java.util.Optional;
import java.util.UUID;

public interface AuthPort {

    AuthLoginView login(AuthLoginCommandDTO request);

    AuthLoginView registerLogin(AuthRegisterLoginCommandDTO request);

    AuthRefreshTokenView refreshToken(AuthRefreshTokenCommandDTO request);

    AuthLogoutView logout(AuthLogoutCommandDTO request);

    void validateToken(AuthValidateTokenQueryDTO request);

    void changePassword(AuthChangePasswordCommandDTO request);

    void resetPassword(AuthResetPasswordCommandDTO request);

    AuthGenerateResetTokenViewDTO generateResetToken(AuthGenerateResetTokenCommandDTO request);

    Optional<UserView> findByUsername(String username);

    Optional<UserView> findByEmail(String email);

    Optional<UserView> findByIdUserExternal(UUID idUserExternal);

    Optional<UserView> findByCodeUser(UUID codeUser);
}
