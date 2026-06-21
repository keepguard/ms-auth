package com.keepguard.ms_auth.infrastructure.persistence.entity;

import lombok.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleIdJpaEntity implements Serializable {
    private UUID userId;
    private UUID roleId;
}
