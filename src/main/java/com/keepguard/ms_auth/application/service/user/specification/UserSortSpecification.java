package com.keepguard.ms_auth.application.service.user.specification;

import com.keepguard.ms_auth.domain.entity.user.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSortSpecification {

    public Specification<User> createSortSpecification(String sortBy, String sortDirection) {
        return (root, query, criteriaBuilder) -> {
            // Por enquanto retorna null, implementar conforme necessário
            // A ordenação será feita pelo Spring Data JPA usando Sort
            return null;
        };
    }

    public Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, "createdAt");
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }
}
