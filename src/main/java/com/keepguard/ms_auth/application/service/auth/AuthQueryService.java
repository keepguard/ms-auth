package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.domain.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthQueryService {

    private final UserRepositoryPort userRepository;

    public Optional<User> findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByIdUserExternal(java.util.UUID idUserExternal) {
        log.debug("Finding user by external ID: {}", idUserExternal);
        return userRepository.findByIdUserExternal(idUserExternal);
    }

    public Optional<User> findByCodeUser(java.util.UUID codeUser) {
        log.debug("Finding user by code: {}", codeUser);
        return userRepository.findByCodeUser(codeUser);
    }
}
