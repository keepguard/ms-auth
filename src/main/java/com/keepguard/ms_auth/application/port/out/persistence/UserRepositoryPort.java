package com.keepguard.ms_auth.application.port.out.persistence;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAll();

    void deleteById(UUID id);

    void delete(User user);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByCodeUser(UUID codeUser);

    Optional<User> findByIdUserExternal(UUID idUserExternal);

    Optional<User> findByIdUserExternalAndTenantId(UUID idUserExternal, UUID tenantId);

    Optional<User> findByUsernameAndStatus(String username, UserStatus status);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    Optional<User> findByUsernameAndCompanyId(String username, UUID companyId);

    Optional<User> findByEmailAndCompanyId(String email, UUID companyId);

    Optional<User> findByUsernameAndTenantId(String username, UUID tenantId);

    Optional<User> findByEmailAndTenantId(String email, UUID tenantId);

    Optional<User> findByCodeUserAndTenantId(UUID codeUser, UUID tenantId);

    List<User> findAllByStatus(UserStatus status);

    long countByCompanyIdAndStatusNot(UUID companyId, UserStatus status);

    Page<User> findAll(Pageable pageable);

    Page<User> findAll(Specification<User> spec, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:id IS NULL OR u.id = :id) AND " +
           "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:idUserExternal IS NULL OR u.idUserExternal = :idUserExternal) AND " +
           "(:codeUser IS NULL OR u.codeUser = :codeUser) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:companyId IS NULL OR u.companyId = :companyId) AND " +
           "(:companyCode IS NULL OR u.companyCode = :companyCode) AND " +
           "(:emailVerified IS NULL OR u.emailVerified = :emailVerified)")
    Page<User> searchUsers(
        @Param("id") UUID id,
        @Param("username") String username,
        @Param("email") String email,
        @Param("idUserExternal") UUID idUserExternal,
        @Param("codeUser") UUID codeUser,
        @Param("status") UserStatus status,
        @Param("companyId") UUID companyId,
        @Param("companyCode") UUID companyCode,
        @Param("emailVerified") Boolean emailVerified,
        Pageable pageable
    );
}

