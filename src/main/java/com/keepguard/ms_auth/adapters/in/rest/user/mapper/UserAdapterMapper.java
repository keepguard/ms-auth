package com.keepguard.ms_auth.adapters.in.rest.user.mapper;

import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.*;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.response.*;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserAdapterMapper {

    // Conversões internas movidas para UserApplicationMapper

    // Conversões internas movidas para UserApplicationMapper

    public UserCreateCommandDTO toCreateCommand(UserCreateRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }

        try {
            return UserCreateCommandDTO.builder()
                    .username(dto.getUsername() != null ? dto.getUsername().trim().toLowerCase() : null)
                    .email(dto.getEmail())
                    .password(dto.getPassword())
                    .name(dto.getUsername()) // Usando username como name
                    .idUserExternal(dto.getIdUserExternal())
                    .codeUser(dto.getCodeUser() != null ? UUID.fromString(dto.getCodeUser()) : null)
                    .companyId(dto.getCompanyId() != null ? UUID.fromString(dto.getCompanyId()) : null)
                    .companyCode(dto.getCompanyCode() != null ? UUID.fromString(dto.getCompanyCode()) : null)
                    .xApplicationUuid(xApplicationUuid)
                    .roles(List.of()) // Roles vazias por padrão
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear UserCreateRequestDTO para UserCreateCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserDeleteCommandDTO toDeleteCommand(String idUserExternal, String reason, UUID xApplicationUuid) {
        try {
            return UserDeleteCommandDTO.builder()
                    .idUserExternal(idUserExternal)
                    .reason(reason)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserDeleteCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserHardDeleteCommandDTO toHardDeleteCommand(String idUserExternal, UUID xApplicationUuid) {
        try {
            return new UserHardDeleteCommandDTO(idUserExternal, xApplicationUuid);
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserHardDeleteCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserBlockCommandDTO toBlockCommand(String idUserExternal, String reason, UUID xApplicationUuid) {
        try {
            return UserBlockCommandDTO.builder()
                    .idUserExternal(idUserExternal)
                    .reason(reason)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserBlockCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserUnlockCommandDTO toUnlockCommand(String idUserExternal, String reason, UUID xApplicationUuid) {
        try {
            return UserUnlockCommandDTO.builder()
                    .idUserExternal(idUserExternal)
                    .reason(reason)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserUnlockCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserValidateEmailCommandDTO toValidateEmailCommand(UserValidateEmailRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }

        try {
            return UserValidateEmailCommandDTO.builder()
                    .idUserExternal(dto.getIdUserExternal())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear UserValidateEmailRequestDTO para UserValidateEmailCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserGetStatusHistoryQueryDTO toGetStatusHistoryQuery(String idUserExternal, Integer page, Integer size, UUID xApplicationUuid) {
        try {
            return UserGetStatusHistoryQueryDTO.builder()
                    .idUserExternal(idUserExternal)
                    .page(page)
                    .size(size)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserGetStatusHistoryQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserGetByCodeQueryDTO toGetByCodeQuery(String codeUser, UUID xApplicationUuid) {
        try {
            return UserGetByCodeQueryDTO.builder()
                    .codeUser(codeUser)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserGetByCodeQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserGetByIdExternalQueryDTO toGetByIdExternalQuery(String idUserExternal, UUID xApplicationUuid) {
        try {
            return UserGetByIdExternalQueryDTO.builder()
                    .idUserExternal(idUserExternal)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserGetByIdExternalQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserGetByEmailQueryDTO toGetByEmailQuery(String email, UUID xApplicationUuid) {
        try {
            return UserGetByEmailQueryDTO.builder()
                    .email(email)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserGetByEmailQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserGetByUsernameQueryDTO toGetByUsernameQuery(String username, UUID xApplicationUuid) {
        try {
            return UserGetByUsernameQueryDTO.builder()
                    .username(username != null ? username.trim().toLowerCase() : null)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserGetByUsernameQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserAddRoleCommandDTO toAddRoleCommand(String idUserExternal, String role, UUID xApplicationUuid) {
        try {
            return UserAddRoleCommandDTO.builder()
                    .idUserExternal(idUserExternal)
                    .role(role)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserAddRoleCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserRemoveRoleCommandDTO toRemoveRoleCommand(String idUserExternal, String role, UUID xApplicationUuid) {
        try {
            return UserRemoveRoleCommandDTO.builder()
                    .idUserExternal(idUserExternal)
                    .role(role)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserRemoveRoleCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserUpdateEmailCommandDTO toUpdateEmailCommand(String idUserExternal, String newEmail, UUID xApplicationUuid) {
        try {
            return UserUpdateEmailCommandDTO.builder()
                    .idUserExternal(idUserExternal)
                    .newEmail(newEmail)
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear dados para UserUpdateEmailCommandDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    public UserSearchQueryDTO toSearchQuery(UserSearchRequestDTO dto, UUID xApplicationUuid) {
        if (dto == null) {
            return null;
        }

        try {
            return UserSearchQueryDTO.builder()
                    .id(dto.getId())
                    .username(dto.getUsername() != null ? dto.getUsername().trim().toLowerCase() : null)
                    .email(dto.getEmail())
                    .idUserExternal(dto.getIdUserExternal())
                    .codeUser(dto.getCodeUser())
                    .status(dto.getStatus())
                    .emailVerified(dto.getEmailVerified())
                    .companyId(dto.getCompanyId())
                    .companyCode(dto.getCompanyCode())
                    .sortBy(dto.getSortBy())
                    .sortDirection(dto.getSortDirection())
                    .page(dto.getPage())
                    .size(dto.getSize())
                    .xApplicationUuid(xApplicationUuid)
                    .build();
        } catch (Exception e) {
            log.error("Erro ao mapear UserSearchRequestDTO para UserSearchQueryDTO: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Conversões internas movidas para UserApplicationMapper

    public UserResponseDTO toResponseDTO(UserView view) {
        if (view == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(UUID.fromString(view.id().toString()));
        dto.setUsername(view.username());
        dto.setEmail(view.email());
        dto.setStatus(view.status() != null ? com.keepguard.ms_auth.domain.enums.UserStatus.valueOf(view.status()) : null);
        dto.setEmailVerified(view.emailVerified() != null ? view.emailVerified() : false);
        dto.setCreatedAt(view.createdAt());
        dto.setUpdatedAt(view.updatedAt());
        dto.setCompanyId(view.companyId());
        dto.setCompanyCode(view.companyCode());
        dto.setXApplication(view.xApplication());
        return dto;
    }

    public UserResponseDTO toResponseDTO(UserSearchView view) {
        if (view == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(UUID.fromString(view.id().toString()));
        dto.setUsername(view.username());
        dto.setEmail(view.email());
        dto.setStatus(view.status() != null ? com.keepguard.ms_auth.domain.enums.UserStatus.valueOf(view.status()) : null);
        dto.setEmailVerified(view.emailVerified() != null ? view.emailVerified() : false);
        dto.setCreatedAt(view.createdAt());
        dto.setUpdatedAt(view.updatedAt());
        dto.setCompanyId(view.companyId());
        dto.setCompanyCode(view.companyCode());
        dto.setXApplication(view.xApplication());
        return dto;
    }

    public UserDetailsResponseDTO toDetailsResponseDTO(UserView view) {
        if (view == null) {
            return null;
        }

        try {
            // Verificar se os campos são válidos antes de converter
            UUID idUserExternalUuid = null;
            if (view.idUserExternal() != null && !view.idUserExternal().isEmpty()) {
                try {
                    idUserExternalUuid = UUID.fromString(view.idUserExternal());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("ID externo inválido: " + view.idUserExternal(), e);
                }
            }

            UUID codeUserUuid = null;
            if (view.codeUser() != null && !view.codeUser().isEmpty()) {
                try {
                    codeUserUuid = UUID.fromString(view.codeUser());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Código do usuário inválido: " + view.codeUser(), e);
                }
            }

            return UserDetailsResponseDTO.builder()
                .id(view.id())
                .idUserExternal(idUserExternalUuid)
                .codeUser(codeUserUuid)
                .username(view.username())
                .email(view.email())
                .status(view.status())
                .emailVerified(view.emailVerified())
                .createdAt(view.createdAt() != null ? view.createdAt().toString() : null)
                .updatedAt(view.updatedAt() != null ? view.updatedAt().toString() : null)
                .lastLogin(view.lastLogin() != null ? view.lastLogin().toString() : null)
                .roles(view.roles())
                .companyId(view.companyId())
                .companyCode(view.companyCode())
                .xApplication(view.xApplication())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter UserView para UserDetailsResponseDTO: " + e.getMessage(), e);
        }
    }

    public UserByCodeResponseDTO toUserByCodeResponseDTO(UserGetByCodeView view) {
        if (view == null) {
            return null;
        }

        try {
            UUID idUserExternalUuid = null;
            if (view.idUserExternal() != null && !view.idUserExternal().isEmpty()) {
                try {
                    idUserExternalUuid = UUID.fromString(view.idUserExternal());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("ID externo inválido: " + view.idUserExternal(), e);
                }
            }

            UUID codeUserUuid = null;
            if (view.codeUser() != null && !view.codeUser().isEmpty()) {
                try {
                    codeUserUuid = UUID.fromString(view.codeUser());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Código do usuário inválido: " + view.codeUser(), e);
                }
            }

            return UserByCodeResponseDTO.builder()
                .id(view.id())
                .idUserExternal(idUserExternalUuid)
                .codeUser(codeUserUuid)
                .username(view.username())
                .email(view.email())
                .status(view.status())
                .emailVerified(view.emailVerified())
                .createdAt(view.createdAt() != null ? view.createdAt().toString() : null)
                .updatedAt(view.updatedAt() != null ? view.updatedAt().toString() : null)
                .lastLogin(view.lastLogin() != null ? view.lastLogin().toString() : null)
                .roles(view.roles())
                .companyId(view.companyId())
                .companyCode(view.companyCode())
                .xApplication(view.xApplication())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter UserGetByCodeView para UserByCodeResponseDTO: " + e.getMessage(), e);
        }
    }

    public UserByIdExternalResponseDTO toUserByIdExternalResponseDTO(UserGetByIdExternalView view) {
        if (view == null) {
            return null;
        }

        try {
            UUID idUserExternalUuid = null;
            if (view.idUserExternal() != null && !view.idUserExternal().isEmpty()) {
                try {
                    idUserExternalUuid = UUID.fromString(view.idUserExternal());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("ID externo inválido: " + view.idUserExternal(), e);
                }
            }

            UUID codeUserUuid = null;
            if (view.codeUser() != null && !view.codeUser().isEmpty()) {
                try {
                    codeUserUuid = UUID.fromString(view.codeUser());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Código do usuário inválido: " + view.codeUser(), e);
                }
            }

            return UserByIdExternalResponseDTO.builder()
                .id(view.id())
                .idUserExternal(idUserExternalUuid)
                .codeUser(codeUserUuid)
                .username(view.username())
                .email(view.email())
                .status(view.status())
                .emailVerified(view.emailVerified())
                .createdAt(view.createdAt() != null ? view.createdAt().toString() : null)
                .updatedAt(view.updatedAt() != null ? view.updatedAt().toString() : null)
                .lastLogin(view.lastLogin() != null ? view.lastLogin().toString() : null)
                .roles(view.roles())
                .companyId(view.companyId())
                .companyCode(view.companyCode())
                .xApplication(view.xApplication())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter UserGetByIdExternalView para UserByIdExternalResponseDTO: " + e.getMessage(), e);
        }
    }

    public UserByEmailResponseDTO toUserByEmailResponseDTO(UserGetByEmailView view) {
        if (view == null) {
            return null;
        }

        try {
            UUID idUserExternalUuid = null;
            if (view.idUserExternal() != null && !view.idUserExternal().isEmpty()) {
                try {
                    idUserExternalUuid = UUID.fromString(view.idUserExternal());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("ID externo inválido: " + view.idUserExternal(), e);
                }
            }

            UUID codeUserUuid = null;
            if (view.codeUser() != null && !view.codeUser().isEmpty()) {
                try {
                    codeUserUuid = UUID.fromString(view.codeUser());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Código do usuário inválido: " + view.codeUser(), e);
                }
            }

            return UserByEmailResponseDTO.builder()
                .id(view.id())
                .idUserExternal(idUserExternalUuid)
                .codeUser(codeUserUuid)
                .username(view.username())
                .email(view.email())
                .status(view.status())
                .emailVerified(view.emailVerified())
                .createdAt(view.createdAt() != null ? view.createdAt().toString() : null)
                .updatedAt(view.updatedAt() != null ? view.updatedAt().toString() : null)
                .lastLogin(view.lastLogin() != null ? view.lastLogin().toString() : null)
                .roles(view.roles())
                .companyId(view.companyId())
                .companyCode(view.companyCode())
                .xApplication(view.xApplication())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter UserGetByEmailView para UserByEmailResponseDTO: " + e.getMessage(), e);
        }
    }

    public UserByUsernameResponseDTO toUserByUsernameResponseDTO(UserGetByUsernameView view) {
        if (view == null) {
            return null;
        }

        try {
            UUID idUserExternalUuid = null;
            if (view.idUserExternal() != null && !view.idUserExternal().isEmpty()) {
                try {
                    idUserExternalUuid = UUID.fromString(view.idUserExternal());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("ID externo inválido: " + view.idUserExternal(), e);
                }
            }

            UUID codeUserUuid = null;
            if (view.codeUser() != null && !view.codeUser().isEmpty()) {
                try {
                    codeUserUuid = UUID.fromString(view.codeUser());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Código do usuário inválido: " + view.codeUser(), e);
                }
            }

            return UserByUsernameResponseDTO.builder()
                .id(view.id())
                .idUserExternal(idUserExternalUuid)
                .codeUser(codeUserUuid)
                .username(view.username())
                .email(view.email())
                .status(view.status())
                .emailVerified(view.emailVerified())
                .createdAt(view.createdAt() != null ? view.createdAt().toString() : null)
                .updatedAt(view.updatedAt() != null ? view.updatedAt().toString() : null)
                .lastLogin(view.lastLogin() != null ? view.lastLogin().toString() : null)
                .roles(view.roles())
                .companyId(view.companyId())
                .companyCode(view.companyCode())
                .xApplication(view.xApplication())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter UserGetByUsernameView para UserByUsernameResponseDTO: " + e.getMessage(), e);
        }
    }

    public UserSearchResponseDTO toSearchResponseDTO(PageResultView<UserSearchView> pageResultView) {
        if (pageResultView == null) {
            return null;
        }

        return UserSearchResponseDTO.builder()
            .content(pageResultView.content().stream()
                .map(this::toDetailsResponseDTOFromSearchView)
                .toList())
            .pageNumber(pageResultView.page())
            .pageSize(pageResultView.size())
            .totalElements(pageResultView.totalElements())
            .totalPages(pageResultView.totalPages())
            .first(pageResultView.first())
            .last(pageResultView.last())
            .hasNext(pageResultView.hasNext())
            .hasPrevious(pageResultView.hasPrevious())
            .build();
    }

    private UserDetailsResponseDTO toDetailsResponseDTOFromSearchView(UserSearchView view) {
        if (view == null) {
            return null;
        }

        try {
            UUID idUserExternalUuid = null;
            if (view.idUserExternal() != null && !view.idUserExternal().isEmpty()) {
                try {
                    idUserExternalUuid = UUID.fromString(view.idUserExternal());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("ID externo inválido: " + view.idUserExternal(), e);
                }
            }

            UUID codeUserUuid = null;
            if (view.codeUser() != null && !view.codeUser().isEmpty()) {
                try {
                    codeUserUuid = UUID.fromString(view.codeUser());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Código do usuário inválido: " + view.codeUser(), e);
                }
            }

            return UserDetailsResponseDTO.builder()
                .id(view.id())
                .idUserExternal(idUserExternalUuid)
                .codeUser(codeUserUuid)
                .username(view.username())
                .email(view.email())
                .status(view.status())
                .emailVerified(view.emailVerified())
                .createdAt(view.createdAt() != null ? view.createdAt().toString() : null)
                .updatedAt(view.updatedAt() != null ? view.updatedAt().toString() : null)
                .lastLogin(view.lastLogin() != null ? view.lastLogin().toString() : null)
                .roles(view.roles())
                .companyId(view.companyId())
                .companyCode(view.companyCode())
                .xApplication(view.xApplication())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao converter UserSearchView para UserDetailsResponseDTO: " + e.getMessage(), e);
        }
    }

    public UserStatusHistoryResponseDTO toStatusHistoryResponseDTO(UserStatusHistory history) {
        if (history == null) {
            return null;
        }

        UserStatusHistoryResponseDTO dto = new UserStatusHistoryResponseDTO();
        dto.setId(history.getId());
        dto.setEventType(history.getEventType());
        dto.setReason(history.getReason());
        dto.setCreatedAt(history.getCreatedAt());
        return dto;
    }

    // Conversões internas movidas para UserApplicationMapper
}
