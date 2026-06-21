package com.keepguard.ms_auth.domain.entity.user;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.keepguard.ms_auth.domain.enums.UserStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private UUID idUserExternal;
    private UUID codeUser;
    private String username;
    private String email;
    private String passwordHash;
    
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @Builder.Default
    private Boolean emailVerified = false;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private UUID companyId;
    private UUID companyCode;
    private UUID xApplication;

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    public static User createNew(String username, String email, String passwordHash,
                                  UUID idUserExternal, UUID codeUser, UUID companyId, UUID companyCode, UUID xApplication) {

        String normalizedUsername = username != null ? username.trim().toLowerCase() : null;
        if (normalizedUsername == null) {
            throw new IllegalArgumentException("Username inválido. Deve ter 3-50 caracteres e conter apenas letras minúsculas, números, ponto e underline.");
        }

        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .username(normalizedUsername)
                .email(email)
                .passwordHash(passwordHash)
                .idUserExternal(idUserExternal)
                .codeUser(codeUser != null ? codeUser : UUID.randomUUID())
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .createdAt(now)
                .updatedAt(now)
                .companyId(companyId)
                .companyCode(companyCode)
                .xApplication(xApplication)
                .build();
    }

    public void markAsDeleted() {
        this.status = UserStatus.DELETED;
        updateTimestamp();
    }

    public void block() {
        this.status = UserStatus.BLOCKED;
        updateTimestamp();
    }

    public void unlock() {
        this.status = UserStatus.ACTIVE;
        updateTimestamp();
    }

    public void validateEmail() {
        this.emailVerified = true;
        updateTimestamp();
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
        this.emailVerified = false;
        updateTimestamp();
    }
}