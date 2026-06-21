package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.role.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepositoryPort {

    Role save(Role role);

    Optional<Role> findById(UUID id);

    List<Role> findAll();

    Page<Role> findAll(Pageable pageable);

    void deleteById(UUID id);

    void delete(Role role);

    Optional<Role> findByName(String name);
}

