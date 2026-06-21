package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserApplicationMapper {

    public UserView toView(User user) {
        if (user == null) {
            return null;
        }

        try {
            return new UserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // name - não disponível na entidade User atual
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
                user.getCodeUser() != null ? user.getCodeUser().toString() : null,
                user.getStatus() != null ? user.getStatus().toString() : null,
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                null, // roles - será preenchido pelo UserQueryService
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserView: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converte User para UserView incluindo as roles do usuário
     * Utilizado pelas operações de query para retornar dados completos do usuário
     */
    public UserView toViewWithRoles(User user, List<String> roles) {
        if (user == null) {
            return null;
        }

        try {
            return new UserView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // name - não disponível na entidade User atual
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
                user.getCodeUser() != null ? user.getCodeUser().toString() : null,
                user.getStatus() != null ? user.getStatus().toString() : null,
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                roles,
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserView com roles: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converte User para UserView incluindo as roles do usuário
     * Versão com valores padrão para campos opcionais para evitar nulls
     * Utilizado em cenários onde é necessário garantir que não haverá campos vazios
     */
    public UserView toViewWithRolesAndDefaults(User user, List<String> roles) {
        if (user == null) {
            return null;
        }

        try {
            return new UserView(
                user.getId(),
                user.getUsername() != null ? user.getUsername() : "",
                user.getEmail() != null ? user.getEmail() : "",
                null, // name - não disponível na entidade User atual
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : "",
                user.getCodeUser() != null ? user.getCodeUser().toString() : "",
                user.getStatus() != null ? user.getStatus().toString() : "ACTIVE",
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                roles,
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserView com roles e defaults: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ========== VIEWS ESPECÍFICAS POR QUERY ==========

    /**
     * Converte User para UserGetByUsernameView incluindo as roles do usuário
     * Usado especificamente pela query findByUsername
     */
    public UserGetByUsernameView toUserGetByUsernameView(User user, List<String> roles) {
        if (user == null) {
            return null;
        }

        try {
            return new UserGetByUsernameView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // name
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
                user.getCodeUser() != null ? user.getCodeUser().toString() : null,
                user.getStatus() != null ? user.getStatus().toString() : null,
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                roles,
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserGetByUsernameView: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converte User para UserGetByEmailView incluindo as roles do usuário
     * Usado especificamente pela query findByEmail
     */
    public UserGetByEmailView toUserGetByEmailView(User user, List<String> roles) {
        if (user == null) {
            return null;
        }

        try {
            return new UserGetByEmailView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // name
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
                user.getCodeUser() != null ? user.getCodeUser().toString() : null,
                user.getStatus() != null ? user.getStatus().toString() : null,
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                roles,
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserGetByEmailView: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converte User para UserGetByCodeView incluindo as roles do usuário
     * Usado especificamente pela query findByCodeUser
     */
    public UserGetByCodeView toUserGetByCodeView(User user, List<String> roles) {
        if (user == null) {
            return null;
        }

        try {
            return new UserGetByCodeView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // name
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
                user.getCodeUser() != null ? user.getCodeUser().toString() : null,
                user.getStatus() != null ? user.getStatus().toString() : null,
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                roles,
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserGetByCodeView: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converte User para UserGetByIdExternalView incluindo as roles do usuário
     * Usado especificamente pela query findByIdUserExternal
     * Versão com defaults para garantir valores não nulos
     */
    public UserGetByIdExternalView toUserGetByIdExternalView(User user, List<String> roles) {
        if (user == null) {
            return null;
        }

        try {
            return new UserGetByIdExternalView(
                user.getId(),
                user.getUsername() != null ? user.getUsername() : "",
                user.getEmail() != null ? user.getEmail() : "",
                null, // name
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : "",
                user.getCodeUser() != null ? user.getCodeUser().toString() : "",
                user.getStatus() != null ? user.getStatus().toString() : "ACTIVE",
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                roles,
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserGetByIdExternalView: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Converte User para UserSearchView incluindo as roles do usuário
     * Usado especificamente pela query searchUsers
     */
    public UserSearchView toUserSearchView(User user, List<String> roles) {
        if (user == null) {
            return null;
        }

        try {
            return new UserSearchView(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // name
                user.getIdUserExternal() != null ? user.getIdUserExternal().toString() : null,
                user.getCodeUser() != null ? user.getCodeUser().toString() : null,
                user.getStatus() != null ? user.getStatus().toString() : null,
                Boolean.TRUE.equals(user.getEmailVerified()),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin(),
                roles,
                user.getCompanyId(),
                user.getCompanyCode(),
                user.getXApplication()
            );
        } catch (Exception e) {
            log.error("Erro ao mapear User para UserSearchView: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    public UserSearchCriteriaView toSearchCriteria(String id, String username, String email, String name,
                                             String idUserExternal, String codeUser, String status,
                                             String role, List<String> roles, String companyId,
                                             String companyCode, Boolean emailVerified,
                                             String createdAtFrom, String createdAtTo,
                                             String updatedAtFrom, String updatedAtTo,
                                             String lastLoginFrom, String lastLoginTo,
                                             Integer page, Integer size, String sortBy, String sortDirection) {
        try {
            return new UserSearchCriteriaView(
                id, username, email, name, idUserExternal, codeUser, status, role, roles,
                companyId, companyCode, emailVerified, 
                null, // createdAtFrom - TODO: converter de String para LocalDateTime
                null, // createdAtTo - TODO: converter de String para LocalDateTime
                null, // updatedAtFrom - TODO: converter de String para LocalDateTime
                null, // updatedAtTo - TODO: converter de String para LocalDateTime
                null, // lastLoginFrom - TODO: converter de String para LocalDateTime
                null, // lastLoginTo - TODO: converter de String para LocalDateTime
                page, size, sortBy, sortDirection
            );
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserSearchCriteriaView: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Page<UserView> toViewPage(Page<User> userPage) {
        if (userPage == null) {
            return Page.empty();
        }

        List<UserView> views = userPage.getContent().stream()
                .map(this::toView)
                .collect(Collectors.toList());

        return new PageImpl<>(views, userPage.getPageable(), userPage.getTotalElements());
    }
}
