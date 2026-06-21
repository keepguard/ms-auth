package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.authority.Authority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthorityRepositoryPort {

    Authority save(Authority authority);

    Optional<Authority> findById(UUID id);

    List<Authority> findAll();

    Page<Authority> findAll(Pageable pageable);

    void deleteById(UUID id);

    void delete(Authority authority);

    Optional<Authority> findByName(String name);
}

