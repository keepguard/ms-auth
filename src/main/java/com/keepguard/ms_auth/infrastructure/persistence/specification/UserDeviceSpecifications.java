package com.keepguard.ms_auth.infrastructure.persistence.specification;

import com.keepguard.ms_auth.infrastructure.persistence.entity.UserDeviceJpaEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class UserDeviceSpecifications {

    private UserDeviceSpecifications() {}

    public static Specification<UserDeviceJpaEntity> withCompanyId(UUID companyId) {
        return (root, query, cb) -> companyId == null ? cb.disjunction() : cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<UserDeviceJpaEntity> withCodeUser(UUID codeUser) {
        return (root, query, cb) -> codeUser == null ? cb.conjunction() : cb.equal(root.get("codeUser"), codeUser);
    }

    public static Specification<UserDeviceJpaEntity> withDeviceId(String deviceId) {
        return (root, query, cb) -> (deviceId == null || deviceId.isBlank())
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("deviceId")), "%" + deviceId.trim().toLowerCase() + "%");
    }

    public static Specification<UserDeviceJpaEntity> notRevoked() {
        return (root, query, cb) -> cb.isNull(root.get("revokedAt"));
    }
}
