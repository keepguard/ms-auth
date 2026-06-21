package com.keepguard.ms_auth.test.builder;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.*;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.response.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * Builder para criar objetos de teste da entidade User
 * Seguindo o padrão usado no ms-company
 */
public class UserTestBuilder {
    
    private UUID id;
    private UUID idUserExternal;
    private UUID codeUser;
    private String username;
    private String email;
    private String passwordHash;
    private UserStatus status;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private UUID companyId;
    private UUID companyCode;
    private UUID xApplication;
    
    private UserTestBuilder() {
        // Valores padrão
        this.id = UUID.randomUUID();
        this.idUserExternal = UUID.randomUUID();
        this.codeUser = UUID.randomUUID();
        this.username = "testuser";
        this.email = "test@example.com";
        this.passwordHash = "hashedpassword";
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true; // Padrão como verificado
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.companyId = UUID.randomUUID();
        this.companyCode = UUID.randomUUID();
        this.xApplication = UUID.randomUUID();
    }
    
    public static UserTestBuilder builder() {
        return new UserTestBuilder();
    }
    
    public static UserTestBuilder aUser() {
        return new UserTestBuilder();
    }
    
    public UserTestBuilder withId(UUID id) {
        this.id = id;
        return this;
    }
    
    public UserTestBuilder withIdUserExternal(UUID idUserExternal) {
        this.idUserExternal = idUserExternal;
        return this;
    }
    
    public UserTestBuilder withCodeUser(UUID codeUser) {
        this.codeUser = codeUser;
        return this;
    }
    
    public UserTestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public UserTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }
    
    public UserTestBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }
    
    public UserTestBuilder withStatus(UserStatus status) {
        this.status = status;
        return this;
    }
    
    public UserTestBuilder withEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
        return this;
    }
    
    public UserTestBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }
    
    public UserTestBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
    
    public UserTestBuilder withLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
        return this;
    }
    
    public UserTestBuilder withCompanyId(UUID companyId) {
        this.companyId = companyId;
        return this;
    }
    
    public UserTestBuilder withCompanyCode(UUID companyCode) {
        this.companyCode = companyCode;
        return this;
    }
    
    public UserTestBuilder withXApplication(UUID xApplication) {
        this.xApplication = xApplication;
        return this;
    }
    
    public UserTestBuilder asActive() {
        this.status = UserStatus.ACTIVE;
        this.emailVerified = true;
        return this;
    }
    
    public UserTestBuilder asBlocked() {
        this.status = UserStatus.BLOCKED;
        return this;
    }
    
    public UserTestBuilder asDeleted() {
        this.status = UserStatus.DELETED;
        return this;
    }
    
    public UserTestBuilder withUnverifiedEmail() {
        this.emailVerified = false;
        return this;
    }
    
    public User buildDomain() {
        return User.builder()
            .id(id)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username(username)
            .email(email)
            .passwordHash(passwordHash)
            .status(status)
            .emailVerified(emailVerified)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .lastLogin(lastLogin)
            .companyId(companyId)
            .companyCode(companyCode)
            .xApplication(xApplication)
            .build();
    }
    
    public UserView buildView() {
        return new UserView(
            id,
            username,
            email,
            null, // name - não está no User domain
            idUserExternal != null ? idUserExternal.toString() : "",
            codeUser != null ? codeUser.toString() : "",
            status != null ? status.toString() : "ACTIVE",
            Boolean.TRUE.equals(emailVerified),
            createdAt,
            updatedAt,
            lastLogin, // lastLogin
            null, // roles - será preenchido pelo serviço
            companyId,
            companyCode,
            xApplication
        );
    }

    // ========== VIEW ESPECÍFICAS BUILDERS ==========

    public UserGetByUsernameView buildGetByUsernameView() {
        return new UserGetByUsernameView(
            id,
            username,
            email,
            null,
            idUserExternal != null ? idUserExternal.toString() : "",
            codeUser != null ? codeUser.toString() : "",
            status != null ? status.toString() : "ACTIVE",
            Boolean.TRUE.equals(emailVerified),
            createdAt,
            updatedAt,
            lastLogin,
            null,
            companyId,
            companyCode,
            xApplication
        );
    }

    public UserGetByEmailView buildGetByEmailView() {
        return new UserGetByEmailView(
            id,
            username,
            email,
            null,
            idUserExternal != null ? idUserExternal.toString() : "",
            codeUser != null ? codeUser.toString() : "",
            status != null ? status.toString() : "ACTIVE",
            Boolean.TRUE.equals(emailVerified),
            createdAt,
            updatedAt,
            lastLogin,
            null,
            companyId,
            companyCode,
            xApplication
        );
    }

    public UserGetByCodeView buildGetByCodeView() {
        return new UserGetByCodeView(
            id,
            username,
            email,
            null,
            idUserExternal != null ? idUserExternal.toString() : "",
            codeUser != null ? codeUser.toString() : "",
            status != null ? status.toString() : "ACTIVE",
            Boolean.TRUE.equals(emailVerified),
            createdAt,
            updatedAt,
            lastLogin,
            null,
            companyId,
            companyCode,
            xApplication
        );
    }

    public UserGetByIdExternalView buildGetByIdExternalView() {
        return new UserGetByIdExternalView(
            id,
            username,
            email,
            null,
            idUserExternal != null ? idUserExternal.toString() : "",
            codeUser != null ? codeUser.toString() : "",
            status != null ? status.toString() : "ACTIVE",
            Boolean.TRUE.equals(emailVerified),
            createdAt,
            updatedAt,
            lastLogin,
            null,
            companyId,
            companyCode,
            xApplication
        );
    }

    public UserSearchView buildSearchView() {
        return new UserSearchView(
            id,
            username,
            email,
            null,
            idUserExternal != null ? idUserExternal.toString() : "",
            codeUser != null ? codeUser.toString() : "",
            status != null ? status.toString() : "ACTIVE",
            Boolean.TRUE.equals(emailVerified),
            createdAt,
            updatedAt,
            lastLogin,
            null,
            companyId,
            companyCode,
            xApplication
        );
    }
    
    // ========== COMMAND DTO BUILDERS ==========
    
    public UserCreateCommandDTO buildCreateCommand() {
        return UserCreateCommandDTO.builder()
            .username(username)
            .email(email)
            .password("password123")
            .name("Test User")
            .idUserExternal(idUserExternal.toString())
            .companyId(companyId)
            .companyCode(companyCode)
            .xApplicationUuid(xApplication)
            .roles(Arrays.asList("ADMIN"))
            .build();
    }
    
    public UserCreateCommandDTO buildCreateCommand(String username, String email, UUID idUserExternal) {
        return UserCreateCommandDTO.builder()
            .username(username)
            .email(email)
            .password("password123")
            .name("Test User")
            .idUserExternal(idUserExternal.toString())
            .companyId(companyId)
            .companyCode(companyCode)
            .xApplicationUuid(xApplication)
            .roles(Arrays.asList("ADMIN"))
            .build();
    }
    
    public UserCreateCommandDTO buildCreateCommandWithNullCompanyId() {
        return UserCreateCommandDTO.builder()
            .username(username)
            .email(email)
            .password("password123")
            .name("Test User")
            .idUserExternal(idUserExternal.toString())
            .companyId(null)
            .companyCode(companyCode)
            .xApplicationUuid(xApplication)
            .roles(Arrays.asList("ADMIN"))
            .build();
    }
    
    public UserCreateCommandDTO buildCreateCommandWithNullCompanyCode() {
        return UserCreateCommandDTO.builder()
            .username(username)
            .email(email)
            .password("password123")
            .name("Test User")
            .idUserExternal(idUserExternal.toString())
            .companyId(companyId)
            .companyCode(null)
            .xApplicationUuid(xApplication)
            .roles(Arrays.asList("ADMIN"))
            .build();
    }
    
    public UserCreateCommandDTO buildCreateCommandWithNullXApplication() {
        return UserCreateCommandDTO.builder()
            .username(username)
            .email(email)
            .password("password123")
            .name("Test User")
            .idUserExternal(idUserExternal.toString())
            .companyId(companyId)
            .companyCode(companyCode)
            .xApplicationUuid(null)
            .roles(Arrays.asList("ADMIN"))
            .build();
    }
    
    public UserDeleteCommandDTO buildDeleteCommand() {
        return UserDeleteCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .reason("Test reason")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserDeleteCommandDTO buildDeleteCommand(UUID idUserExternal) {
        return UserDeleteCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .reason("Test reason")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserBlockCommandDTO buildBlockCommand() {
        return UserBlockCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .reason("Test reason")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserBlockCommandDTO buildBlockCommand(UUID idUserExternal) {
        return UserBlockCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .reason("Test reason")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserUnlockCommandDTO buildUnlockCommand() {
        return UserUnlockCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .reason("Test reason")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserUnlockCommandDTO buildUnlockCommand(UUID idUserExternal) {
        return UserUnlockCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .reason("Test reason")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserValidateEmailCommandDTO buildValidateEmailCommand() {
        return UserValidateEmailCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserValidateEmailCommandDTO buildValidateEmailCommand(UUID idUserExternal) {
        return UserValidateEmailCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserAddRoleCommandDTO buildAddRoleCommand() {
        return UserAddRoleCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .role("ADMIN")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserAddRoleCommandDTO buildAddRoleCommand(UUID idUserExternal, String role) {
        return UserAddRoleCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .role(role)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserRemoveRoleCommandDTO buildRemoveRoleCommand() {
        return UserRemoveRoleCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .role("ADMIN")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserRemoveRoleCommandDTO buildRemoveRoleCommand(UUID idUserExternal, String role) {
        return UserRemoveRoleCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .role(role)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserUpdateEmailCommandDTO buildUpdateEmailCommand() {
        return UserUpdateEmailCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .newEmail("newemail@example.com")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserUpdateEmailCommandDTO buildUpdateEmailCommand(UUID idUserExternal, String newEmail) {
        return UserUpdateEmailCommandDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .newEmail(newEmail)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    // ========== QUERY DTO BUILDERS ==========
    
    public UserGetByUsernameQueryDTO buildGetByUsernameCommand() {
        return UserGetByUsernameQueryDTO.builder()
            .username(username)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserGetByUsernameQueryDTO buildGetByUsernameCommand(String username) {
        return UserGetByUsernameQueryDTO.builder()
            .username(username)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserGetByEmailQueryDTO buildGetByEmailCommand() {
        return UserGetByEmailQueryDTO.builder()
            .email(email)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserGetByEmailQueryDTO buildGetByEmailCommand(String email) {
        return UserGetByEmailQueryDTO.builder()
            .email(email)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserGetByIdExternalQueryDTO buildGetByIdExternalCommand() {
        return UserGetByIdExternalQueryDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserGetByIdExternalQueryDTO buildGetByIdExternalCommand(UUID idUserExternal) {
        return UserGetByIdExternalQueryDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserGetStatusHistoryQueryDTO buildGetStatusHistoryCommand() {
        return UserGetStatusHistoryQueryDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .page(0)
            .size(10)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserGetStatusHistoryQueryDTO buildGetStatusHistoryCommand(UUID idUserExternal, int page, int size) {
        return UserGetStatusHistoryQueryDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .page(page)
            .size(size)
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserSearchQueryDTO buildSearchCommand() {
        return UserSearchQueryDTO.builder()
            .username(username)
            .email(email)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .companyId(companyId)
            .companyCode(companyCode)
            .page(0)
            .size(10)
            .sortBy("username")
            .sortDirection("ASC")
            .xApplicationUuid(xApplication)
            .build();
    }
    
    public UserSearchQueryDTO buildSearchCommandWithCriteria(UserSearchCriteriaView criteria) {
        return UserSearchQueryDTO.builder()
            .username(criteria.username())
            .email(criteria.email())
            .idUserExternal(criteria.idUserExternal() != null ? UUID.fromString(criteria.idUserExternal()) : null)
            .codeUser(criteria.codeUser() != null ? UUID.fromString(criteria.codeUser()) : null)
            .companyId(criteria.companyId() != null ? UUID.fromString(criteria.companyId()) : null)
            .companyCode(criteria.companyCode() != null ? UUID.fromString(criteria.companyCode()) : null)
            .page(criteria.page())
            .size(criteria.size())
            .sortBy(criteria.sortBy())
            .sortDirection(criteria.sortDirection())
            .xApplicationUuid(xApplication)
            .build();
    }
    
    // ========== REQUEST DTO BUILDERS ==========
    
    public UserCreateRequestDTO buildCreateRequestDTO() {
        return UserCreateRequestDTO.builder()
            .username(username)
            .email(email)
            .password("password123")
            .idUserExternal(idUserExternal.toString())
            .codeUser(codeUser.toString())
            .companyId(companyId.toString())
            .companyCode(companyCode.toString())
            .xApplication(xApplication.toString())
            .build();
    }
    
    public UserValidateEmailRequestDTO buildValidateEmailRequestDTO() {
        return UserValidateEmailRequestDTO.builder()
            .idUserExternal(idUserExternal.toString())
            .build();
    }
    
    public UserUpdateEmailRequestDTO buildUpdateEmailRequestDTO() {
        return UserUpdateEmailRequestDTO.builder()
            .newEmail("newemail@example.com")
            .build();
    }
    
    public UserSearchRequestDTO buildSearchRequestDTO() {
        return UserSearchRequestDTO.builder()
            .username(username)
            .email(email)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .companyId(companyId)
            .companyCode(companyCode)
            .status(status)
            .emailVerified(emailVerified)
            .page(0)
            .size(10)
            .sortBy("createdAt")
            .sortDirection("DESC")
            .build();
    }
    
    public UserAddRoleToUserRequestDTO buildAddRoleToUserRequestDTO() {
        return UserAddRoleToUserRequestDTO.builder()
            .role("ROLE_ADMIN")
            .build();
    }
    
    public UserStatusReasonRequestDTO buildStatusReasonRequestDTO() {
        return UserStatusReasonRequestDTO.builder()
            .reason("Test reason")
            .build();
    }
    
    // ========== RESPONSE DTO BUILDERS ==========
    
    public UserResponseDTO buildResponseDTO() {
        return UserResponseDTO.builder()
            .id(id)
            .username(username)
            .email(email)
            .status(status)
            .emailVerified(emailVerified != null ? emailVerified : false)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .companyId(companyId)
            .companyCode(companyCode)
            .xApplication(xApplication)
            .build();
    }
    
    public UserDetailsResponseDTO buildDetailsResponseDTO() {
        return UserDetailsResponseDTO.builder()
            .id(id)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username(username)
            .email(email)
            .status(status != null ? status.toString() : "ACTIVE")
            .emailVerified(emailVerified)
            .createdAt(createdAt != null ? createdAt.toString() : null)
            .updatedAt(updatedAt != null ? updatedAt.toString() : null)
            .lastLogin(lastLogin != null ? lastLogin.toString() : null)
            .roles(Arrays.asList("ROLE_USER"))
            .companyId(companyId)
            .companyCode(companyCode)
            .xApplication(xApplication)
            .build();
    }
    
    public UserSearchResponseDTO buildSearchResponseDTO() {
        return UserSearchResponseDTO.builder()
            .content(Arrays.asList(buildDetailsResponseDTO()))
            .pageNumber(0)
            .pageSize(10)
            .totalElements(1L)
            .totalPages(1)
            .first(true)
            .last(true)
            .hasNext(false)
            .hasPrevious(false)
            .build();
    }
}