package com.keepguard.ms_auth.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRoleIdJpaEntity implements Serializable {
    private UUID companyId;
    private UUID roleId;
}
