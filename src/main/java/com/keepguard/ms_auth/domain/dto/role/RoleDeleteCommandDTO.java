package com.keepguard.ms_auth.domain.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDeleteCommandDTO {

    private UUID id;
    private UUID xApplicationUuid;
}

