package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.mapper.UserApplicationMapper;
import com.keepguard.ms_auth.application.port.out.cache.UserCachePort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.UserStatusHistoryRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import com.keepguard.ms_auth.domain.enums.UserStatusEventType;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Query Service Tests")
class UserQueryServiceTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private UserStatusHistoryRepositoryPort userStatusHistoryRepository;
    @Mock private UserRoleRepositoryPort userRoleRepository;
    @Mock private RoleRepositoryPort roleRepository;
    @Mock private UserApplicationMapper userMapper;
    @Mock private MetricsPort metricsPort;
    @Mock private UserCachePort userCachePort;

    @InjectMocks private UserQueryService userQueryService;

    private User user;
    private UserView userView;
    private UserGetByUsernameView userGetByUsernameView;
    private UserGetByEmailView userGetByEmailView;
    private UserGetByIdExternalView userGetByIdExternalView;
    private UserSearchView userSearchView;
    private UserStatusHistory userStatusHistory;
    private UUID userId;
    private UUID idUserExternal;
    private UserTestBuilder userTestBuilder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        idUserExternal = UUID.randomUUID();
        
        userTestBuilder = UserTestBuilder.builder()
            .withId(userId)
            .withIdUserExternal(idUserExternal);
            
        user = userTestBuilder.buildDomain();
        userView = userTestBuilder.buildView();
        userGetByUsernameView = userTestBuilder.buildGetByUsernameView();
        userGetByEmailView = userTestBuilder.buildGetByEmailView();
        userGetByIdExternalView = userTestBuilder.buildGetByIdExternalView();
        userSearchView = userTestBuilder.buildSearchView();
            
        userStatusHistory = UserStatusHistory.builder()
                .userId(user.getId())
            .eventType(UserStatusEventType.CREATED)
            .reason("Test reason")
            .createdAt(LocalDateTime.now())
            .build();
            
            
        // Mock para UserRoleRepository e RoleRepository - usando lenient para evitar UnnecessaryStubbingException
        lenient().when(userRoleRepository.findByUserId(any(UUID.class)))
            .thenReturn(List.of());
            
        lenient().when(roleRepository.findById(any(UUID.class)))
            .thenReturn(Optional.empty());
    }

    @Test
    @DisplayName("Deve encontrar usuário por username com sucesso")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        var command = userTestBuilder.buildGetByUsernameCommand("testuser");
        when(userRepository.findByUsernameAndXApplication("testuser", command.getXApplicationUuid()))
            .thenReturn(Optional.of(user));
        when(userMapper.toUserGetByUsernameView(eq(user), anyList())).thenReturn(userGetByUsernameView);

        // When
        UserGetByUsernameView result = userQueryService.findByUsername(command);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        verify(userRepository).findByUsernameAndXApplication("testuser", command.getXApplicationUuid());
        verify(userMapper).toUserGetByUsernameView(eq(user), anyList());
        verify(userCachePort).cacheUserByUsername("testuser", userGetByUsernameView);
        verify(metricsPort, times(2)).incrementCounter(eq("user_queries_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado por username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        var command = userTestBuilder.buildGetByUsernameCommand("testuser");
        when(userRepository.findByUsernameAndXApplication("testuser", command.getXApplicationUuid()))
            .thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> userQueryService.findByUsername(command));
        
        assertEquals("Usuário não encontrado: testuser", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    @DisplayName("Deve encontrar usuário por email com sucesso")
    void shouldFindUserByEmailSuccessfully() {
        // Given
        var command = userTestBuilder.buildGetByEmailCommand("test@example.com");
        when(userRepository.findByEmailAndXApplication("test@example.com", command.getXApplicationUuid()))
            .thenReturn(Optional.of(user));
        when(userMapper.toUserGetByEmailView(eq(user), anyList())).thenReturn(userGetByEmailView);

        // When
        UserGetByEmailView result = userQueryService.findByEmail(command);

        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        verify(userRepository).findByEmailAndXApplication("test@example.com", command.getXApplicationUuid());
        verify(userMapper).toUserGetByEmailView(eq(user), anyList());
        verify(userCachePort).cacheUserByEmail("test@example.com", userGetByEmailView);
        verify(metricsPort, times(2)).incrementCounter(eq("user_queries_total"), any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado por email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Given
        var command = userTestBuilder.buildGetByEmailCommand("test@example.com");
        when(userRepository.findByEmailAndXApplication("test@example.com", command.getXApplicationUuid()))
            .thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> userQueryService.findByEmail(command));
        
        assertEquals("Usuário não encontrado: test@example.com", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    @DisplayName("Deve encontrar usuário por ID externo com sucesso")
    void shouldFindUserByIdUserExternalSuccessfully() {
        // Given
        when(userRepository.findByIdUserExternal(idUserExternal)).thenReturn(Optional.of(user));
        when(userMapper.toUserGetByIdExternalView(eq(user), anyList())).thenReturn(userGetByIdExternalView);

        // When
        var command = userTestBuilder.buildGetByIdExternalCommand(idUserExternal);
        UserGetByIdExternalView result = userQueryService.findByIdUserExternal(command);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        verify(userRepository).findByIdUserExternal(idUserExternal);
        verify(userMapper).toUserGetByIdExternalView(eq(user), anyList());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado por ID externo")
    void shouldThrowExceptionWhenUserNotFoundByIdUserExternal() {
        // Given
        when(userRepository.findByIdUserExternal(idUserExternal)).thenReturn(Optional.empty());

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> {
                var command = userTestBuilder.buildGetByIdExternalCommand(idUserExternal);
                userQueryService.findByIdUserExternal(command);
            });
        
        assertEquals("Usuário não encontrado: " + idUserExternal, exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    @DisplayName("Deve buscar histórico de status do usuário com sucesso")
    void shouldGetUserStatusHistorySuccessfully() {
        // Given
        Page<UserStatusHistory> historyPage = new PageImpl<>(List.of(userStatusHistory));
        when(userRepository.findByIdUserExternal(idUserExternal)).thenReturn(Optional.of(user));
        when(userStatusHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
            .thenReturn(List.of(userStatusHistory));

        // When
        var command = userTestBuilder.buildGetStatusHistoryCommand(idUserExternal, 0, 1);
        PageResultView<UserStatusHistory> result = userQueryService.getUserStatusHistory(command);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getSize());
        assertEquals(1L, result.getTotalElements());
        verify(userRepository).findByIdUserExternal(idUserExternal);
        verify(userStatusHistoryRepository).findByUserIdOrderByCreatedAtDesc(user.getId());
        verify(metricsPort, times(1)).incrementCounter(eq("user_queries_total"), any());
    }

    @Test
    @DisplayName("Deve buscar usuários com critérios de busca com sucesso")
    void shouldSearchUsersSuccessfully() {
        // Given
        UserSearchCriteriaView criteria = new UserSearchCriteriaView(
            null, "test", "test@example.com", null, null, null, "ACTIVE", null, null, null, null, null, null, null, null, null, null, null, 0, 10, "username", "ASC"
        );
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.searchUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(userPage);
        when(userMapper.toUserSearchView(eq(user), anyList())).thenReturn(userSearchView);

        // When
        var command = userTestBuilder.buildSearchCommandWithCriteria(criteria);
        PageResultView<UserSearchView> result = userQueryService.searchUsers(command);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(1, result.getSize());
        assertEquals(1L, result.getTotalElements());
        verify(userRepository).searchUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(userMapper).toUserSearchView(eq(user), anyList());
        verify(metricsPort, times(2)).incrementCounter(eq("user_queries_total"), any());
    }

    @Test
    @DisplayName("Deve buscar usuários com critérios vazios")
    void shouldSearchUsersWithEmptyCriteria() {
        // Given
        UserSearchCriteriaView criteria = new UserSearchCriteriaView(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 0, 10, "username", "ASC"
        );
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.searchUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(userPage);
        when(userMapper.toUserSearchView(eq(user), anyList())).thenReturn(userSearchView);

        // When
        var command = userTestBuilder.buildSearchCommandWithCriteria(criteria);
        PageResultView<UserSearchView> result = userQueryService.searchUsers(command);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).searchUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(userMapper).toUserSearchView(eq(user), anyList());
    }


    @Test
    @DisplayName("Deve buscar usuários com paginação na primeira página")
    void shouldSearchUsersWithPaginationFirstPage() {
        // Given
        UserSearchCriteriaView criteria = new UserSearchCriteriaView(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 0, 5, "username", "ASC"
        );
        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 5), 1L);
        when(userRepository.searchUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(userPage);
        when(userMapper.toUserSearchView(eq(user), anyList())).thenReturn(userSearchView);

        // When
        var command = userTestBuilder.buildSearchCommandWithCriteria(criteria);
        PageResultView<UserSearchView> result = userQueryService.searchUsers(command);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPageNumber());
        assertEquals(5, result.getSize());
        assertEquals(1L, result.getTotalElements());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertFalse(result.hasPrevious());
    }

    @Test
    @DisplayName("Deve buscar usuários com paginação na última página")
    void shouldSearchUsersWithPaginationLastPage() {
        // Given
        UserSearchCriteriaView criteria = new UserSearchCriteriaView(
            null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 1, 5, "username", "ASC"
        );
        Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(1, 5), 6L);
        when(userRepository.searchUsers(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(userPage);
        when(userMapper.toUserSearchView(eq(user), anyList())).thenReturn(userSearchView);

        // When
        var command = userTestBuilder.buildSearchCommandWithCriteria(criteria);
        PageResultView<UserSearchView> result = userQueryService.searchUsers(command);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getPageNumber());
        assertEquals(5, result.getSize());
        assertEquals(6L, result.getTotalElements());
        assertFalse(result.isFirst());
        assertTrue(result.isLast());
        assertFalse(result.hasNext());
        assertTrue(result.hasPrevious());
    }
}
