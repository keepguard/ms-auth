package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStatusHistoryRepositoryPort {
    UserStatusHistory save(UserStatusHistory userStatusHistory);
    Optional<UserStatusHistory> findById(UUID id);
    List<UserStatusHistory> findAll();
    void deleteById(UUID id);
    void delete(UserStatusHistory userStatusHistory);
    List<UserStatusHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    void deleteByUserId(UUID userId);
}

