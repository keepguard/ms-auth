package com.keepguard.ms_auth.domain.entity.user.specifications;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserSpecifications {

    public static Specification<User> withDynamicFilters(
            UUID id,
            UUID idUserExternal,
            UUID codeUser,
            String username,
            String email,
            UserStatus status,
            Boolean emailVerified,
            UUID companyId,
            LocalDateTime createdAtStart,
            LocalDateTime createdAtEnd,
            LocalDateTime updatedAtStart,
            LocalDateTime updatedAtEnd,
            LocalDateTime lastLoginStart,
            LocalDateTime lastLoginEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (id != null) predicates.add(cb.equal(root.get("id"), id));
            if (idUserExternal != null) predicates.add(cb.equal(root.get("idUserExternal"), idUserExternal));
            if (codeUser != null) predicates.add(cb.equal(root.get("codeUser"), codeUser));
            if (username != null && !username.isBlank())
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            if (email != null && !email.isBlank())
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (emailVerified != null) predicates.add(cb.equal(root.get("emailVerified"), emailVerified));
            if (companyId != null) predicates.add(cb.equal(root.get("companyId"), companyId));

            if (createdAtStart != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtStart));
            if (createdAtEnd != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtEnd));

            if (updatedAtStart != null) predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedAtStart));
            if (updatedAtEnd != null) predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), updatedAtEnd));

            if (lastLoginStart != null) predicates.add(cb.greaterThanOrEqualTo(root.get("lastLogin"), lastLoginStart));
            if (lastLoginEnd != null) predicates.add(cb.lessThanOrEqualTo(root.get("lastLogin"), lastLoginEnd));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}