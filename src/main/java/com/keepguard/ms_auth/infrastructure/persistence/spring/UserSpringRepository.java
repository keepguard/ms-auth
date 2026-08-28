package com.keepguard.ms_auth.infrastructure.persistence.spring;

import com.keepguard.ms_auth.infrastructure.persistence.entity.UserJpaEntity;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserSpringRepository extends JpaRepository<UserJpaEntity, UUID>, JpaSpecificationExecutor<UserJpaEntity> {
    Optional<UserJpaEntity> findByUsername(String username);
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByCodeUser(UUID codeUser);
    Optional<UserJpaEntity> findByIdUserExternal(UUID idUserExternal);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.idUserExternal = :idUserExternal AND u.tenantId = :tenantId")
    Optional<UserJpaEntity> findByIdUserExternalAndTenantId(@Param("idUserExternal") UUID idUserExternal, @Param("tenantId") UUID tenantId);
    
    Optional<UserJpaEntity> findByUsernameAndStatus(String username, UserStatus status);
    Optional<UserJpaEntity> findByEmailAndStatus(String email, UserStatus status);
    @Query("SELECT u FROM UserJpaEntity u WHERE u.username = :username AND u.companyId = :companyId")
    Optional<UserJpaEntity> findByUsernameAndCompanyId(@Param("username") String username, @Param("companyId") UUID companyId);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.companyId = :companyId")
    Optional<UserJpaEntity> findByEmailAndCompanyId(@Param("email") String email, @Param("companyId") UUID companyId);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.username = :username AND u.tenantId = :tenantId")
    Optional<UserJpaEntity> findByUsernameAndTenantId(@Param("username") String username, @Param("tenantId") UUID tenantId);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.tenantId = :tenantId")
    Optional<UserJpaEntity> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") UUID tenantId);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.codeUser = :codeUser AND u.tenantId = :tenantId")
    Optional<UserJpaEntity> findByCodeUserAndTenantId(@Param("codeUser") UUID codeUser, @Param("tenantId") UUID tenantId);

    @Query("SELECT u FROM UserJpaEntity u WHERE u.codeUser = :codeUser AND u.companyId = :companyId")
    Optional<UserJpaEntity> findByCodeUserAndCompanyId(@Param("codeUser") UUID codeUser, @Param("companyId") UUID companyId);
    
    List<UserJpaEntity> findAllByStatus(UserStatus status);

    long countByCompanyIdAndStatusNot(UUID companyId, UserStatus status);
}
