package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.mapper.UserApplicationMapper;
import com.keepguard.ms_auth.application.port.out.cache.UserCachePort;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserStatusHistoryRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.application.service.exception.QueryOperationException;
import com.keepguard.ms_auth.domain.dto.user.*;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserQueryService {

    private final UserRepositoryPort userRepository;
    private final UserStatusHistoryRepositoryPort userStatusHistoryRepository;
    private final UserRoleRepositoryPort userRoleRepository;
    private final RoleRepositoryPort roleRepository;
    private final MetricsPort metricsPort;
    private final UserCachePort userCachePort;
    private final UserApplicationMapper userMapper;

    public UserGetByUsernameView findByUsername(UserGetByUsernameQueryDTO query) {
        User user = userRepository.findByUsernameAndXApplication(query.getUsername(), query.getXApplicationUuid())
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + query.getUsername(),
                "USER_NOT_FOUND", Map.of("username", query.getUsername())));

        List<String> userRoles = getUserRoles(user.getId());

        UserGetByUsernameView userView = userMapper.toUserGetByUsernameView(user, userRoles);
        userCachePort.cacheUserByUsername(query.getUsername(), userView);

        metricsPort.incrementCounter("user_queries_total",
            Map.of("query_type", "find_by_username", "status", "success"));

        return userView;
    }

    public UserGetByEmailView findByEmail(UserGetByEmailQueryDTO query) {

        User user = userRepository.findByEmailAndXApplication(query.getEmail(), query.getXApplicationUuid())
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + query.getEmail(),
                "USER_NOT_FOUND", Map.of("email", query.getEmail())));

        List<String> userRoles = getUserRoles(user.getId());

        UserGetByEmailView userView = userMapper.toUserGetByEmailView(user, userRoles);
        userCachePort.cacheUserByEmail(query.getEmail(), userView);

        metricsPort.incrementCounter("user_queries_total",
            Map.of("query_type", "find_by_email", "status", "success"));

        return userView;

    }

    public UserGetByCodeView findByCodeUser(UserGetByCodeQueryDTO query) {
        UUID codeUserUuid = UUID.fromString(query.getCodeUser());
        User user = userRepository.findByCodeUserAndXApplication(codeUserUuid, query.getXApplicationUuid())
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + query.getCodeUser(),
                "USER_NOT_FOUND", Map.of("codeUser", query.getCodeUser())));

        List<String> userRoles = getUserRoles(user.getId());

        UserGetByCodeView userView = userMapper.toUserGetByCodeView(user, userRoles);
        userCachePort.cacheUserByCodeUser(query.getCodeUser(), userView);

        metricsPort.incrementCounter("user_queries_total",
            Map.of("query_type", "find_by_code", "status", "success"));

        return userView;
    }

    public UserGetByIdExternalView findByIdUserExternal(UserGetByIdExternalQueryDTO query) {

        UUID idUserExternalUuid = UUID.fromString(query.getIdUserExternal());

        User user = userRepository.findByIdUserExternal(idUserExternalUuid)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + query.getIdUserExternal(),
                "USER_NOT_FOUND", Map.of("idUserExternal", query.getIdUserExternal())));

        List<String> userRoles = getUserRoles(user.getId());

        UserGetByIdExternalView userView = userMapper.toUserGetByIdExternalView(user, userRoles);

        metricsPort.incrementCounter("user_queries_total",
            Map.of("query_type", "find_by_external_id", "status", "success"));

        return userView;

    }

    public PageResultView<UserStatusHistory> getUserStatusHistory(UserGetStatusHistoryQueryDTO query) {

        UUID idUserExternalUuid = UUID.fromString(query.getIdUserExternal());

        User user = userRepository.findByIdUserExternal(idUserExternalUuid)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + query.getIdUserExternal(),
                "USER_NOT_FOUND", Map.of("idUserExternal", query.getIdUserExternal())));

        Pageable pageable = PageRequest.of(query.getPage() != null ? query.getPage() : 0,
            query.getSize() != null ? query.getSize() : 10);
        List<UserStatusHistory> historyList = userStatusHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // Aplicar paginação manualmente
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), historyList.size());
        List<UserStatusHistory> paginatedList = historyList.subList(start, end);

        Page<UserStatusHistory> historyPage = new org.springframework.data.domain.PageImpl<>(paginatedList, pageable, historyList.size());

        metricsPort.incrementCounter("user_queries_total",
            Map.of("query_type", "status_history", "status", "success"));

        return PageResultView.<UserStatusHistory>builder()
            .content(historyPage.getContent())
            .page(historyPage.getNumber())
            .size(historyPage.getSize())
            .totalElements(historyPage.getTotalElements())
            .totalPages(historyPage.getTotalPages())
            .first(historyPage.isFirst())
            .last(historyPage.isLast())
            .hasNext(historyPage.hasNext())
            .hasPrevious(historyPage.hasPrevious())
            .build();
    }

    public PageResultView<UserSearchView> searchUsers(UserSearchQueryDTO query) {
        log.info("🔍 Buscando usuários com critérios: {}", query);

        Sort sort = Sort.by(Sort.Direction.fromString(
            query.getSortDirection() != null ? query.getSortDirection() : "ASC"),
            query.getSortBy() != null ? query.getSortBy() : "createdAt"
        );

        Pageable pageable = PageRequest.of(
            query.getPage() != null ? query.getPage() : 0,
            query.getSize() != null ? query.getSize() : 10,
            sort
        );

        Page<User> userPage = userRepository.searchUsers(
            query.getId(),
            query.getUsername(),
            query.getEmail(),
            query.getIdUserExternal(),
            query.getCodeUser(),
            query.getStatus(),
            query.getCompanyId(),
            query.getCompanyCode(),
            query.getEmailVerified(),
            pageable
        );

        metricsPort.incrementCounter("user_queries_total",
            Map.of("query_type", "search", "status", "success"));

        List<UserSearchView> userViews = userPage.getContent().stream()
            .map(user -> {
                List<String> userRoles = getUserRoles(user.getId());
                return userMapper.toUserSearchView(user, userRoles);
            })
            .toList();

        return PageResultView.<UserSearchView>builder()
            .content(userViews)
            .page(userPage.getNumber())
            .size(userPage.getSize())
            .totalElements(userPage.getTotalElements())
            .totalPages(userPage.getTotalPages())
            .first(userPage.isFirst())
            .last(userPage.isLast())
            .hasNext(userPage.hasNext())
            .hasPrevious(userPage.hasPrevious())
            .build();

    }

    public List<String> getUserRoles(UUID userId) {

        List<UUID> roleIds = userRoleRepository.findByUserId(userId)
            .stream()
            .map(userRole -> userRole.getRoleId())
            .toList();

        List<String> roleNames = roleIds.stream()
            .map(roleId -> roleRepository.findById(roleId))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(role -> role.getName())
            .toList();

        metricsPort.incrementCounter("user_queries_total",
            Map.of("query_type", "get_user_roles", "status", "success"));

        return roleNames;
    }
}
