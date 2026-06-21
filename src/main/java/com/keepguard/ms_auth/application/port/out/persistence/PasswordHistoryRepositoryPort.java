package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.user.PasswordHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordHistoryRepositoryPort {
    PasswordHistory save(PasswordHistory passwordHistory);
    Optional<PasswordHistory> findById(UUID id);
    List<PasswordHistory> findAll();
    void deleteById(UUID id);
    void delete(PasswordHistory passwordHistory);
    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<PasswordHistory> findTop5ByUserIdOrderByCreatedAtDesc(UUID userId);
}

