package com.keepguard.ms_auth.application.service.auth;

import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.domain.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthQueryService {

    private final UserRepositoryPort userRepository;

    public Optional<User> findByUsername(String username, UUID companyId) {
        log.debug("Finding user by username and company: {}", username);
        return userRepository.findByUsernameAndCompanyId(username, companyId);
    }

    public Optional<User> findByEmail(String email, UUID companyId) {
        log.debug("Finding user by email and company: {}", email);
        return userRepository.findByEmailAndCompanyId(email, companyId);
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
