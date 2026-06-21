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
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.idUserExternal = :idUserExternal AND u.xApplication = :xApplication")
    Optional<UserJpaEntity> findByIdUserExternalAndXApplication(@Param("idUserExternal") UUID idUserExternal, @Param("xApplication") UUID xApplication);
    
    Optional<UserJpaEntity> findByUsernameAndStatus(String username, UserStatus status);
    Optional<UserJpaEntity> findByEmailAndStatus(String email, UserStatus status);
    @Query("SELECT u FROM UserJpaEntity u WHERE u.username = :username AND u.xApplication = :xApplication")
    Optional<UserJpaEntity> findByUsernameAndXApplication(@Param("username") String username, @Param("xApplication") UUID xApplication);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.email = :email AND u.xApplication = :xApplication")
    Optional<UserJpaEntity> findByEmailAndXApplication(@Param("email") String email, @Param("xApplication") UUID xApplication);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.codeUser = :codeUser AND u.xApplication = :xApplication")
    Optional<UserJpaEntity> findByCodeUserAndXApplication(@Param("codeUser") UUID codeUser, @Param("xApplication") UUID xApplication);
    
    List<UserJpaEntity> findAllByStatus(UserStatus status);
    
}
