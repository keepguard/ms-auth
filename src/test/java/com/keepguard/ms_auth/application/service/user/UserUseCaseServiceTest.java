package com.keepguard.ms_auth.application.service.user;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.dto.user.UserCreateCommandDTO;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UserUseCaseService
 */
@ExtendWith(MockitoExtension.class)
class UserUseCaseServiceTest {
    
    @Mock
    private UserCommandService commandService;
    
    @Mock
    private UserQueryService queryService;
    
    @InjectMocks
    private UserUseCaseService userUseCaseService;
    
    private UserCreateCommandDTO userCreateCommand;
    private UserView userView;
    private UserGetByUsernameView userGetByUsernameView;
    private UserGetByEmailView userGetByEmailView;
    private UserGetByIdExternalView userGetByIdExternalView;
    private UserSearchCriteriaView searchCriteria;
    private PageResultView<UserStatusHistory> statusHistoryPage;
    private UUID idUserExternal;
    private String reason;
    private UserTestBuilder userTestBuilder;
    
    @BeforeEach
    void setUp() {
        idUserExternal = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        reason = "Test reason";
        
        userTestBuilder = UserTestBuilder.builder()
            .withIdUserExternal(idUserExternal);
            
        userCreateCommand = userTestBuilder.buildCreateCommand();
        
        userView = new UserView(
            UUID.randomUUID(),
            "testuser",
            "test@example.com",
            "Test User",
            idUserExternal.toString(),
            "code-123",
            "ACTIVE",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now(), // lastLogin
            List.of("ROLE_USER"),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID() // xApplication
        );

        userGetByUsernameView = userTestBuilder.buildGetByUsernameView();
        userGetByEmailView = userTestBuilder.buildGetByEmailView();
        userGetByIdExternalView = userTestBuilder.buildGetByIdExternalView();
        
        searchCriteria = new UserSearchCriteriaView(
            null, // id
            "testuser",
            "test@example.com",
            "Test User",
            idUserExternal.toString(),
            null, // codeUser
            "ACTIVE",
            "ROLE_USER",
            null, // roles (List)
            UUID.randomUUID().toString(),
            null, // companyCode
            null, // emailVerified
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            null, // updatedAtFrom
            null, // updatedAtTo
            null, // lastLoginFrom
            null, // lastLoginTo
            0,
            10,
            "createdAt",
            "DESC"
        );
        
        statusHistoryPage = new PageResultView<>(
            List.of(),
            0,
            10,
            0L,
            0,
            true,
            true,
            false,
            false
        );
    }
    
    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        // Given
        when(commandService.create(userCreateCommand)).thenReturn(userView);
        
        // When
        UserView result = userUseCaseService.create(userCreateCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(userView, result);
        
        verify(commandService, times(1)).create(userCreateCommand);
    }
    
    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        // Given
        var deleteCommand = userTestBuilder.buildDeleteCommand();
        doNothing().when(commandService).delete(deleteCommand);
        
        // When
        userUseCaseService.delete(deleteCommand);
        
        // Then
        verify(commandService, times(1)).delete(deleteCommand);
    }
    
    @Test
    @DisplayName("Deve bloquear usuário com sucesso")
    void shouldBlockUserSuccessfully() {
        // Given
        var blockCommand = userTestBuilder.buildBlockCommand();
        doNothing().when(commandService).block(blockCommand);
        
        // When
        userUseCaseService.block(blockCommand);
        
        // Then
        verify(commandService, times(1)).block(blockCommand);
    }
    
    @Test
    @DisplayName("Deve desbloquear usuário com sucesso")
    void shouldUnlockUserSuccessfully() {
        // Given
        var unlockCommand = userTestBuilder.buildUnlockCommand();
        doNothing().when(commandService).unlock(unlockCommand);
        
        // When
        userUseCaseService.unlock(unlockCommand);
        
        // Then
        verify(commandService, times(1)).unlock(unlockCommand);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por username com sucesso")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        String username = "testuser";
        var command = userTestBuilder.buildGetByUsernameCommand(username);
        when(queryService.findByUsername(command)).thenReturn(userGetByUsernameView);
        
        // When
        UserGetByUsernameView result = userUseCaseService.findByUsername(command);
        
        // Then
        assertNotNull(result);
        assertEquals(userGetByUsernameView, result);
        
        verify(queryService, times(1)).findByUsername(command);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void shouldFindUserByEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        var command = userTestBuilder.buildGetByEmailCommand(email);
        when(queryService.findByEmail(command)).thenReturn(userGetByEmailView);
        
        // When
        UserGetByEmailView result = userUseCaseService.findByEmail(command);
        
        // Then
        assertNotNull(result);
        assertEquals(userGetByEmailView, result);
        
        verify(queryService, times(1)).findByEmail(command);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por ID externo com sucesso")
    void shouldFindUserByIdUserExternalSuccessfully() {
        // Given
        var command = userTestBuilder.buildGetByIdExternalCommand();
        when(queryService.findByIdUserExternal(command)).thenReturn(userGetByIdExternalView);
        
        // When
        UserGetByIdExternalView result = userUseCaseService.findByIdUserExternal(command);
        
        // Then
        assertNotNull(result);
        assertEquals(userGetByIdExternalView, result);
        
        verify(queryService, times(1)).findByIdUserExternal(command);
    }
    
    @Test
    @DisplayName("Deve buscar histórico de status do usuário com sucesso")
    void shouldGetUserStatusHistorySuccessfully() {
        // Given
        Integer page = 0;
        Integer size = 10;
        var command = userTestBuilder.buildGetStatusHistoryCommand(idUserExternal, page, size);
        when(queryService.getUserStatusHistory(command)).thenReturn(statusHistoryPage);
        
        // When
        PageResultView<UserStatusHistory> result = userUseCaseService.getUserStatusHistory(command);
        
        // Then
        assertNotNull(result);
        assertEquals(statusHistoryPage, result);
        
        verify(queryService, times(1)).getUserStatusHistory(command);
    }
    
    @Test
    @DisplayName("Deve adicionar role ao usuário com sucesso")
    void shouldAddRoleToUserSuccessfully() {
        // Given
        String roleName = "ROLE_ADMIN";
        var command = userTestBuilder.buildAddRoleCommand(idUserExternal, roleName);
        doNothing().when(commandService).addRoleToUser(command);
        
        // When
        userUseCaseService.addRoleToUser(command);
        
        // Then
        verify(commandService, times(1)).addRoleToUser(command);
    }
    
    @Test
    @DisplayName("Deve remover role do usuário com sucesso")
    void shouldRemoveRoleFromUserSuccessfully() {
        // Given
        String roleName = "ROLE_ADMIN";
        var command = userTestBuilder.buildRemoveRoleCommand(idUserExternal, roleName);
        doNothing().when(commandService).removeRoleFromUser(command);
        
        // When
        userUseCaseService.removeRoleFromUser(command);
        
        // Then
        verify(commandService, times(1)).removeRoleFromUser(command);
    }
    
    
    @Test
    @DisplayName("Deve buscar usuários com critérios com sucesso")
    void shouldSearchUsersSuccessfully() {
        // Given
        UserSearchView searchView = userTestBuilder.buildSearchView();
        PageResultView<UserSearchView> searchResult = new PageResultView<>(
            List.of(searchView),
            0,
            10,
            1L,
            1,
            true,
            true,
            false,
            false
        );
        var command = userTestBuilder.buildSearchCommandWithCriteria(searchCriteria);
        when(queryService.searchUsers(command)).thenReturn(searchResult);
        
        // When
        PageResultView<UserSearchView> result = userUseCaseService.searchUsers(command);
        
        // Then
        assertNotNull(result);
        assertEquals(searchResult, result);
        
        verify(queryService, times(1)).searchUsers(command);
    }
    
    @Test
    @DisplayName("Deve atualizar email do usuário com sucesso")
    void shouldUpdateUserEmailSuccessfully() {
        // Given
        String newEmail = "newemail@example.com";
        var command = userTestBuilder.buildUpdateEmailCommand(idUserExternal, newEmail);
        doNothing().when(commandService).updateUserEmail(command);
        
        // When
        userUseCaseService.updateUserEmail(command);
        
        // Then
        verify(commandService, times(1)).updateUserEmail(command);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante criação de usuário")
    void shouldHandleExceptionsDuringUserCreation() {
        // Given
        when(commandService.create(any())).thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userUseCaseService.create(userCreateCommand);
        });
        
        verify(commandService, times(1)).create(userCreateCommand);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante busca de usuário")
    void shouldHandleExceptionsDuringUserSearch() {
        // Given
        String username = "testuser";
        var command = userTestBuilder.buildGetByUsernameCommand(username);
        when(queryService.findByUsername(command)).thenThrow(new RuntimeException("Service error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userUseCaseService.findByUsername(command);
        });
        
        verify(queryService, times(1)).findByUsername(command);
    }
    
    @Test
    @DisplayName("Deve lidar com exceções durante operações de comando")
    void shouldHandleExceptionsDuringCommandOperations() {
        // Given
        var deleteCommand = userTestBuilder.buildDeleteCommand();
        doThrow(new RuntimeException("Service error")).when(commandService).delete(any());
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userUseCaseService.delete(deleteCommand);
        });
        
        verify(commandService, times(1)).delete(deleteCommand);
    }
}
