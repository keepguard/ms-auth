package com.keepguard.ms_auth.domain.dto.user;

import com.keepguard.ms_auth.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchQueryDTO {

    private UUID id;

    private String username;

    private String email;

    private UUID idUserExternal;

    private UUID codeUser;

    private UserStatus status;

    private Boolean emailVerified;

    private UUID companyId;

    private UUID companyCode;

    private String sortBy;

    private String sortDirection;

    private Integer page;

    private Integer size;

    private UUID xApplicationUuid;
}

