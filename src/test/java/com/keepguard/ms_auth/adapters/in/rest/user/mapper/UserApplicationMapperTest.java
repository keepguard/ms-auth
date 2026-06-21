package com.keepguard.ms_auth.adapters.in.rest.user.mapper;

import com.keepguard.ms_auth.application.dto.user.*;
import com.keepguard.ms_auth.application.mapper.UserApplicationMapper;
import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para UserApplicationMapper
 * Cobertura: 100% dos métodos públicos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Application Mapper Tests")
class UserApplicationMapperTest {
    
    @InjectMocks
    private UserApplicationMapper userApplicationMapper;
    
    private UUID userId;
    private User user;
    private Pageable pageable;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        // Criar usuário de teste usando builder
        user = UserTestBuilder.builder()
            .withId(userId)
            .buildDomain();
        
        pageable = PageRequest.of(0, 10);
    }
    
    @Test
    @DisplayName("Deve converter User para UserView com sucesso")
    void shouldConvertUserToUserViewSuccessfully() {
        // When
        UserView result = userApplicationMapper.toView(user);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertNull(result.name()); // name não disponível na entidade User atual
        assertEquals(user.getIdUserExternal().toString(), result.idUserExternal());
        assertEquals(user.getCodeUser().toString(), result.codeUser());
        assertEquals(user.getStatus().toString(), result.status());
        assertEquals(user.getEmailVerified(), result.emailVerified());
        assertEquals(user.getCreatedAt(), result.createdAt());
        assertEquals(user.getUpdatedAt(), result.updatedAt());
        assertNull(result.roles()); // roles não implementado
    }
    
    @Test
    @DisplayName("Deve converter User nulo para UserView nulo")
    void shouldConvertNullUserToNullUserView() {
        // When
        UserView result = userApplicationMapper.toView(null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve converter User com campos nulos")
    void shouldConvertUserWithNullFields() {
        // Given
        User userWithNulls = User.builder()
            .id(userId)
            .idUserExternal(null)
            .codeUser(null)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedpassword")
            .status(null)
            .emailVerified(null)
            .createdAt(null)
            .updatedAt(null)
            .build();
        
        // When
        UserView result = userApplicationMapper.toView(userWithNulls);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        assertNull(result.name());
        assertNull(result.idUserExternal());
        assertNull(result.codeUser());
        assertNull(result.status());
        assertEquals(false, result.emailVerified());
        assertNull(result.createdAt());
        assertNull(result.updatedAt());
        assertNull(result.roles());
    }
    
    @Test
    @DisplayName("Deve converter User com valores específicos")
    void shouldConvertUserWithSpecificValues() {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 11, 30, 0);
        UUID idUserExternal = UUID.randomUUID();
        UUID codeUser = UUID.randomUUID();
        
        User specificUser = User.builder()
            .id(userId)
            .idUserExternal(idUserExternal)
            .codeUser(codeUser)
            .username("specificuser")
            .email("specific@example.com")
            .passwordHash("hashedpassword")
            .status(UserStatus.BLOCKED)
            .emailVerified(true)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
        
        // When
        UserView result = userApplicationMapper.toView(specificUser);
        
        // Then
        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("specificuser", result.username());
        assertEquals("specific@example.com", result.email());
        assertNull(result.name());
        assertEquals(idUserExternal.toString(), result.idUserExternal());
        assertEquals(codeUser.toString(), result.codeUser());
        assertEquals("BLOCKED", result.status());
        assertEquals(true, result.emailVerified());
        assertEquals(createdAt, result.createdAt());
        assertEquals(updatedAt, result.updatedAt());
        assertNull(result.roles());
    }
    
    @Test
    @DisplayName("Deve converter Page<User> para Page<UserView> com sucesso")
    void shouldConvertUserPageToUserViewPageSuccessfully() {
        // Given
        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, pageable, 1L);
        
        // When
        Page<UserView> result = userApplicationMapper.toViewPage(userPage);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(10, result.getSize());
        assertEquals(0, result.getNumber());
        
        UserView userView = result.getContent().get(0);
        assertEquals(user.getId(), userView.id());
        assertEquals(user.getUsername(), userView.username());
        assertEquals(user.getEmail(), userView.email());
    }
    
    @Test
    @DisplayName("Deve converter Page<User> nula para Page<UserView> vazia")
    void shouldConvertNullUserPageToEmptyUserViewPage() {
        // When
        Page<UserView> result = userApplicationMapper.toViewPage(null);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getContent().size());
        assertEquals(0L, result.getTotalElements());
    }
    
    @Test
    @DisplayName("Deve converter Page<User> vazia para Page<UserView> vazia")
    void shouldConvertEmptyUserPageToEmptyUserViewPage() {
        // Given
        Page<User> emptyUserPage = new PageImpl<>(List.of(), pageable, 0L);
        
        // When
        Page<UserView> result = userApplicationMapper.toViewPage(emptyUserPage);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getContent().size());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }
    
    @Test
    @DisplayName("Deve converter Page<User> com múltiplos usuários")
    void shouldConvertUserPageWithMultipleUsers() {
        // Given
        User user2 = UserTestBuilder.builder()
            .withId(UUID.randomUUID())
            .withUsername("user2")
            .withEmail("user2@example.com")
            .buildDomain();
        
        List<User> users = List.of(user, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, 2L);
        
        // When
        Page<UserView> result = userApplicationMapper.toViewPage(userPage);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        
        UserView userView1 = result.getContent().get(0);
        assertEquals(user.getId(), userView1.id());
        assertEquals(user.getUsername(), userView1.username());
        
        UserView userView2 = result.getContent().get(1);
        assertEquals(user2.getId(), userView2.id());
        assertEquals(user2.getUsername(), userView2.username());
    }
    
    @Test
    @DisplayName("Deve converter Page<User> com paginação em página intermediária")
    void shouldConvertUserPageWithPaginationInMiddlePage() {
        // Given
        Pageable middlePageable = PageRequest.of(2, 5); // Página 2, tamanho 5
        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users, middlePageable, 11L); // Total 11 elementos
        
        // When
        Page<UserView> result = userApplicationMapper.toViewPage(userPage);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(11L, result.getTotalElements());
        assertEquals(3, result.getTotalPages()); // 11 elementos / 5 por página = 3 páginas
        assertEquals(5, result.getSize());
        assertEquals(2, result.getNumber());
    }
    
    @Test
    @DisplayName("Deve converter User com emailVerified true")
    void shouldConvertUserWithEmailVerifiedTrue() {
        // Given
        User userWithEmailVerified = User.builder()
            .id(userId)
            .idUserExternal(UUID.randomUUID())
            .codeUser(UUID.randomUUID())
            .username("verifieduser")
            .email("verified@example.com")
            .passwordHash("hashedpassword")
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When
        UserView result = userApplicationMapper.toView(userWithEmailVerified);
        
        // Then
        assertNotNull(result);
        assertEquals(true, result.emailVerified());
    }
    
    @Test
    @DisplayName("Deve converter User com emailVerified false")
    void shouldConvertUserWithEmailVerifiedFalse() {
        // Given
        User userWithEmailNotVerified = User.builder()
            .id(userId)
            .idUserExternal(UUID.randomUUID())
            .codeUser(UUID.randomUUID())
            .username("unverifieduser")
            .email("unverified@example.com")
            .passwordHash("hashedpassword")
            .status(UserStatus.ACTIVE)
            .emailVerified(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When
        UserView result = userApplicationMapper.toView(userWithEmailNotVerified);
        
        // Then
        assertNotNull(result);
        assertEquals(false, result.emailVerified());
    }
    
    @Test
    @DisplayName("Deve converter User com diferentes status")
    void shouldConvertUserWithDifferentStatus() {
        // Given
        User userWithStatus = User.builder()
            .id(userId)
            .idUserExternal(UUID.randomUUID())
            .codeUser(UUID.randomUUID())
            .username("statususer")
            .email("status@example.com")
            .passwordHash("hashedpassword")
            .status(UserStatus.BLOCKED)
            .emailVerified(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When
        UserView result = userApplicationMapper.toView(userWithStatus);
        
        // Then
        assertNotNull(result);
        assertEquals("BLOCKED", result.status());
    }
    
    @Test
    @DisplayName("Deve converter User com username e email vazios")
    void shouldConvertUserWithEmptyUsernameAndEmail() {
        // Given
        User userWithEmptyFields = User.builder()
            .id(userId)
            .idUserExternal(UUID.randomUUID())
            .codeUser(UUID.randomUUID())
            .username("")
            .email("")
            .passwordHash("hashedpassword")
            .status(UserStatus.ACTIVE)
            .emailVerified(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When
        UserView result = userApplicationMapper.toView(userWithEmptyFields);
        
        // Then
        assertNotNull(result);
        assertEquals("", result.username());
        assertEquals("", result.email());
    }

    @Test
    @DisplayName("Deve converter User para UserView com roles")
    void shouldConvertUserToUserViewWithRoles() {
        // Given
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        
        // When
        UserView result = userApplicationMapper.toViewWithRoles(user, roles);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertEquals(user.getIdUserExternal().toString(), result.idUserExternal());
        assertEquals(user.getCodeUser().toString(), result.codeUser());
        assertEquals(user.getStatus().toString(), result.status());
        assertEquals(user.getEmailVerified(), result.emailVerified());
        assertNotNull(result.roles());
        assertEquals(2, result.roles().size());
        assertTrue(result.roles().contains("ROLE_USER"));
        assertTrue(result.roles().contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Deve converter User nulo para UserView nulo com roles")
    void shouldConvertNullUserToNullUserViewWithRoles() {
        // When
        UserView result = userApplicationMapper.toViewWithRoles(null, List.of());
        
        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter User com roles vazias")
    void shouldConvertUserWithEmptyRoles() {
        // When
        UserView result = userApplicationMapper.toViewWithRoles(user, List.of());
        
        // Then
        assertNotNull(result);
        assertNotNull(result.roles());
        assertEquals(0, result.roles().size());
    }

    @Test
    @DisplayName("Deve converter User para UserView com roles e defaults")
    void shouldConvertUserToUserViewWithRolesAndDefaults() {
        // Given
        List<String> roles = List.of("ROLE_USER");
        
        // When
        UserView result = userApplicationMapper.toViewWithRolesAndDefaults(user, roles);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertEquals(user.getIdUserExternal().toString(), result.idUserExternal());
        assertEquals(user.getCodeUser().toString(), result.codeUser());
        assertEquals(user.getStatus().toString(), result.status());
        assertEquals(user.getEmailVerified(), result.emailVerified());
        assertNotNull(result.roles());
        assertEquals(1, result.roles().size());
    }

    @Test
    @DisplayName("Deve converter User com campos nulos usando defaults")
    void shouldConvertUserWithNullFieldsUsingDefaults() {
        // Given
        User userWithNulls = User.builder()
            .id(userId)
            .idUserExternal(null)
            .codeUser(null)
            .username(null)
            .email(null)
            .passwordHash("hashedpassword")
            .status(null)
            .emailVerified(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        List<String> roles = List.of("ROLE_USER");
        
        // When
        UserView result = userApplicationMapper.toViewWithRolesAndDefaults(userWithNulls, roles);
        
        // Then
        assertNotNull(result);
        assertEquals("", result.username()); // Default vazio
        assertEquals("", result.email()); // Default vazio
        assertEquals("", result.idUserExternal()); // Default vazio
        assertEquals("", result.codeUser()); // Default vazio
        assertEquals("ACTIVE", result.status()); // Default ACTIVE
        assertEquals(false, result.emailVerified()); // null vira false
        assertNotNull(result.roles());
        assertEquals(1, result.roles().size());
    }

    @Test
    @DisplayName("Deve converter User nulo para UserView nulo com roles e defaults")
    void shouldConvertNullUserToNullUserViewWithRolesAndDefaults() {
        // When
        UserView result = userApplicationMapper.toViewWithRolesAndDefaults(null, List.of());
        
        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter User para UserGetByUsernameView com roles")
    void shouldConvertUserToUserGetByUsernameViewWithRoles() {
        // Given
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        
        // When
        UserGetByUsernameView result = userApplicationMapper.toUserGetByUsernameView(user, roles);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertEquals(2, result.roles().size());
        assertTrue(result.roles().contains("ROLE_USER"));
        assertTrue(result.roles().contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Deve retornar null ao converter User nulo para UserGetByUsernameView")
    void shouldReturnNullWhenConvertingNullUserToUserGetByUsernameView() {
        // When
        UserGetByUsernameView result = userApplicationMapper.toUserGetByUsernameView(null, List.of());
        
        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter User para UserGetByEmailView com roles")
    void shouldConvertUserToUserGetByEmailViewWithRoles() {
        // Given
        List<String> roles = List.of("ROLE_USER");
        
        // When
        UserGetByEmailView result = userApplicationMapper.toUserGetByEmailView(user, roles);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertEquals(1, result.roles().size());
    }

    @Test
    @DisplayName("Deve retornar null ao converter User nulo para UserGetByEmailView")
    void shouldReturnNullWhenConvertingNullUserToUserGetByEmailView() {
        // When
        UserGetByEmailView result = userApplicationMapper.toUserGetByEmailView(null, List.of());
        
        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter User para UserGetByCodeView com roles")
    void shouldConvertUserToUserGetByCodeViewWithRoles() {
        // Given
        List<String> roles = List.of("ROLE_ADMIN");
        
        // When
        UserGetByCodeView result = userApplicationMapper.toUserGetByCodeView(user, roles);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertEquals(1, result.roles().size());
        assertTrue(result.roles().contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Deve retornar null ao converter User nulo para UserGetByCodeView")
    void shouldReturnNullWhenConvertingNullUserToUserGetByCodeView() {
        // When
        UserGetByCodeView result = userApplicationMapper.toUserGetByCodeView(null, List.of());
        
        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter User para UserGetByIdExternalView com roles e defaults")
    void shouldConvertUserToUserGetByIdExternalViewWithRolesAndDefaults() {
        // Given
        List<String> roles = List.of("ROLE_USER");
        
        // When
        UserGetByIdExternalView result = userApplicationMapper.toUserGetByIdExternalView(user, roles);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertEquals(1, result.roles().size());
    }

    @Test
    @DisplayName("Deve converter User com campos nulos para UserGetByIdExternalView usando defaults")
    void shouldConvertUserWithNullFieldsToUserGetByIdExternalViewUsingDefaults() {
        // Given
        User userWithNulls = User.builder()
            .id(userId)
            .idUserExternal(null)
            .codeUser(null)
            .username(null)
            .email(null)
            .passwordHash("hashedpassword")
            .status(null)
            .emailVerified(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        List<String> roles = List.of("ROLE_USER");
        
        // When
        UserGetByIdExternalView result = userApplicationMapper.toUserGetByIdExternalView(userWithNulls, roles);
        
        // Then
        assertNotNull(result);
        assertEquals("", result.username()); // Default vazio
        assertEquals("", result.email()); // Default vazio
        assertEquals("ACTIVE", result.status()); // Default ACTIVE
        assertEquals(1, result.roles().size());
    }

    @Test
    @DisplayName("Deve retornar null ao converter User nulo para UserGetByIdExternalView")
    void shouldReturnNullWhenConvertingNullUserToUserGetByIdExternalView() {
        // When
        UserGetByIdExternalView result = userApplicationMapper.toUserGetByIdExternalView(null, List.of());
        
        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Deve converter User para UserSearchView com roles")
    void shouldConvertUserToUserSearchViewWithRoles() {
        // Given
        List<String> roles = List.of("ROLE_USER", "ROLE_MANAGER");
        
        // When
        UserSearchView result = userApplicationMapper.toUserSearchView(user, roles);
        
        // Then
        assertNotNull(result);
        assertEquals(user.getId(), result.id());
        assertEquals(user.getUsername(), result.username());
        assertEquals(user.getEmail(), result.email());
        assertEquals(2, result.roles().size());
        assertTrue(result.roles().contains("ROLE_USER"));
        assertTrue(result.roles().contains("ROLE_MANAGER"));
    }

    @Test
    @DisplayName("Deve retornar null ao converter User nulo para UserSearchView")
    void shouldReturnNullWhenConvertingNullUserToUserSearchView() {
        // When
        UserSearchView result = userApplicationMapper.toUserSearchView(null, List.of());
        
        // Then
        assertNull(result);
    }
}
