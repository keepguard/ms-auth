package com.keepguard.ms_auth.infrastructure.persistence;

import com.keepguard.ms_auth.domain.entity.user.User;
import com.keepguard.ms_auth.domain.enums.UserStatus;
import com.keepguard.ms_auth.infrastructure.persistence.entity.UserJpaEntity;
import com.keepguard.ms_auth.infrastructure.persistence.mapper.UserJpaMapper;
import com.keepguard.ms_auth.infrastructure.persistence.spring.UserSpringRepository;
import com.keepguard.ms_auth.test.builder.UserTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UserRepositoryAdapter
 */
@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {
    
    @Mock
    private UserSpringRepository jpaRepository;
    
    @Mock
    private UserJpaMapper userJpaMapper;
    
    @InjectMocks
    private UserRepositoryAdapter userRepositoryAdapter;
    
    private User user;
    private UserJpaEntity userJpaEntity;
    private UUID userId;
    
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        user = UserTestBuilder.aUser()
            .withId(userId)
            .asActive()
            .buildDomain();
            
        userJpaEntity = UserJpaEntity.builder()
            .id(user.getId())
            .idUserExternal(user.getIdUserExternal())
            .codeUser(user.getCodeUser())
            .username(user.getUsername())
            .email(user.getEmail())
            .passwordHash(user.getPasswordHash())
            .status(user.getStatus())
            .emailVerified(user.getEmailVerified())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLogin(user.getLastLogin())
            .build();
    }
    
    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void shouldSaveUserSuccessfully() {
        // Given
        when(userJpaMapper.toJpaEntity(user)).thenReturn(userJpaEntity);
        when(jpaRepository.save(userJpaEntity)).thenReturn(userJpaEntity);
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        User savedUser = userRepositoryAdapter.save(user);
        
        // Then
        assertNotNull(savedUser);
        assertEquals(user, savedUser);
        verify(userJpaMapper, times(1)).toJpaEntity(user);
        verify(jpaRepository, times(1)).save(userJpaEntity);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldFindUserByIdSuccessfully() {
        // Given
        when(jpaRepository.findById(userId)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findById(userId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findById(userId);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por ID")
    void shouldReturnEmptyOptionalWhenUserNotFoundById() {
        // Given
        when(jpaRepository.findById(userId)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findById(userId);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findById(userId);
    }
    
    @Test
    @DisplayName("Deve buscar todos os usuários com sucesso")
    void shouldFindAllUsersSuccessfully() {
        // Given
        List<UserJpaEntity> userJpaEntities = List.of(userJpaEntity);
        when(jpaRepository.findAll()).thenReturn(userJpaEntities);
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        List<User> result = userRepositoryAdapter.findAll();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(jpaRepository, times(1)).findAll();
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve deletar usuário por ID com sucesso")
    void shouldDeleteUserByIdSuccessfully() {
        // Given
        doNothing().when(jpaRepository).deleteById(userId);
        
        // When
        userRepositoryAdapter.deleteById(userId);
        
        // Then
        verify(jpaRepository, times(1)).deleteById(userId);
    }
    
    @Test
    @DisplayName("Deve deletar usuário com sucesso")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userJpaMapper.toJpaEntity(user)).thenReturn(userJpaEntity);
        doNothing().when(jpaRepository).delete(userJpaEntity);
        
        // When
        userRepositoryAdapter.delete(user);
        
        // Then
        verify(userJpaMapper, times(1)).toJpaEntity(user);
        verify(jpaRepository, times(1)).delete(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por username com sucesso")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        String username = "testuser";
        when(jpaRepository.findByUsername(username)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findByUsername(username);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findByUsername(username);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por username")
    void shouldReturnEmptyOptionalWhenUserNotFoundByUsername() {
        // Given
        String username = "nonexistentuser";
        when(jpaRepository.findByUsername(username)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findByUsername(username);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByUsername(username);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void shouldFindUserByEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findByEmail(email);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findByEmail(email);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por email")
    void shouldReturnEmptyOptionalWhenUserNotFoundByEmail() {
        // Given
        String email = "nonexistent@example.com";
        when(jpaRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findByEmail(email);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByEmail(email);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por código de usuário com sucesso")
    void shouldFindUserByCodeUserSuccessfully() {
        // Given
        UUID codeUser = UUID.randomUUID();
        when(jpaRepository.findByCodeUser(codeUser)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findByCodeUser(codeUser);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findByCodeUser(codeUser);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por código de usuário")
    void shouldReturnEmptyOptionalWhenUserNotFoundByCodeUser() {
        // Given
        UUID codeUser = UUID.randomUUID();
        when(jpaRepository.findByCodeUser(codeUser)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findByCodeUser(codeUser);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByCodeUser(codeUser);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por ID externo com sucesso")
    void shouldFindUserByIdUserExternalSuccessfully() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        when(jpaRepository.findByIdUserExternal(idUserExternal)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findByIdUserExternal(idUserExternal);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findByIdUserExternal(idUserExternal);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por ID externo")
    void shouldReturnEmptyOptionalWhenUserNotFoundByIdUserExternal() {
        // Given
        UUID idUserExternal = UUID.randomUUID();
        when(jpaRepository.findByIdUserExternal(idUserExternal)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findByIdUserExternal(idUserExternal);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByIdUserExternal(idUserExternal);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por username e status com sucesso")
    void shouldFindUserByUsernameAndStatusSuccessfully() {
        // Given
        String username = "testuser";
        UserStatus status = UserStatus.ACTIVE;
        when(jpaRepository.findByUsernameAndStatus(username, status)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findByUsernameAndStatus(username, status);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findByUsernameAndStatus(username, status);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por username e status")
    void shouldReturnEmptyOptionalWhenUserNotFoundByUsernameAndStatus() {
        // Given
        String username = "testuser";
        UserStatus status = UserStatus.BLOCKED;
        when(jpaRepository.findByUsernameAndStatus(username, status)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findByUsernameAndStatus(username, status);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByUsernameAndStatus(username, status);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por email e status com sucesso")
    void shouldFindUserByEmailAndStatusSuccessfully() {
        // Given
        String email = "test@example.com";
        UserStatus status = UserStatus.ACTIVE;
        when(jpaRepository.findByEmailAndStatus(email, status)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findByEmailAndStatus(email, status);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findByEmailAndStatus(email, status);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por email e status")
    void shouldReturnEmptyOptionalWhenUserNotFoundByEmailAndStatus() {
        // Given
        String email = "test@example.com";
        UserStatus status = UserStatus.BLOCKED;
        when(jpaRepository.findByEmailAndStatus(email, status)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findByEmailAndStatus(email, status);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByEmailAndStatus(email, status);
    }
    
    @Test
    @DisplayName("Deve buscar usuário por email e xApplication com sucesso")
    void shouldFindUserByEmailAndXApplicationSuccessfully() {
        // Given
        String email = "test@example.com";
        UUID xApplication = UUID.randomUUID();
        when(jpaRepository.findByEmailAndXApplication(email, xApplication)).thenReturn(Optional.of(userJpaEntity));
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        Optional<User> result = userRepositoryAdapter.findByEmailAndXApplication(email, xApplication);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(jpaRepository, times(1)).findByEmailAndXApplication(email, xApplication);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não encontrado por email e xApplication")
    void shouldReturnEmptyOptionalWhenUserNotFoundByEmailAndXApplication() {
        // Given
        String email = "test@example.com";
        UUID xApplication = UUID.randomUUID();
        when(jpaRepository.findByEmailAndXApplication(email, xApplication)).thenReturn(Optional.empty());
        
        // When
        Optional<User> result = userRepositoryAdapter.findByEmailAndXApplication(email, xApplication);
        
        // Then
        assertTrue(result.isEmpty());
        verify(jpaRepository, times(1)).findByEmailAndXApplication(email, xApplication);
    }
    
    @Test
    @DisplayName("Deve buscar todos os usuários por status com sucesso")
    void shouldFindAllUsersByStatusSuccessfully() {
        // Given
        UserStatus status = UserStatus.ACTIVE;
        List<UserJpaEntity> userJpaEntities = List.of(userJpaEntity);
        when(jpaRepository.findAllByStatus(status)).thenReturn(userJpaEntities);
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(user);
        
        // When
        List<User> result = userRepositoryAdapter.findAllByStatus(status);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
        verify(jpaRepository, times(1)).findAllByStatus(status);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    
    @Test
    @DisplayName("Deve lidar com exceções do JPA repository")
    void shouldHandleJpaRepositoryExceptions() {
        // Given
        when(jpaRepository.findById(userId)).thenThrow(new RuntimeException("Database error"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userRepositoryAdapter.findById(userId);
        });
        
        verify(jpaRepository, times(1)).findById(userId);
    }
    
    @Test
    @DisplayName("Deve testar UserJpaMapper.toEntity com dados válidos")
    void shouldTestUserJpaMapperToEntityWithValidData() {
        // Given
        User domainUser = UserTestBuilder.aUser()
            .withId(userId)
            .withUsername("testuser")
            .withEmail("test@example.com")
            .asActive()
            .buildDomain();
        
        when(userJpaMapper.toJpaEntity(domainUser)).thenReturn(userJpaEntity);
        
        // When
        UserJpaEntity result = userJpaMapper.toJpaEntity(domainUser);
        
        // Then
        assertNotNull(result);
        assertEquals(domainUser.getId(), result.getId());
        assertEquals(domainUser.getUsername(), result.getUsername());
        assertEquals(domainUser.getEmail(), result.getEmail());
        assertEquals(domainUser.getStatus(), result.getStatus());
        
        verify(userJpaMapper, times(1)).toJpaEntity(domainUser);
    }
    
    @Test
    @DisplayName("Deve testar UserJpaMapper.toEntity com null")
    void shouldTestUserJpaMapperToEntityWithNull() {
        // Given
        when(userJpaMapper.toJpaEntity(null)).thenReturn(null);
        
        // When
        UserJpaEntity result = userJpaMapper.toJpaEntity(null);
        
        // Then
        assertNull(result);
        verify(userJpaMapper, times(1)).toJpaEntity(null);
    }
    
    @Test
    @DisplayName("Deve testar UserJpaMapper.toDomain com dados válidos")
    void shouldTestUserJpaMapperToDomainWithValidData() {
        // Given
        User entityUser = UserTestBuilder.aUser()
            .withId(userId)
            .withUsername("testuser")
            .withEmail("test@example.com")
            .asActive()
            .buildDomain();
        
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(entityUser);
        
        // When
        User result = userJpaMapper.toDomain(userJpaEntity);
        
        // Then
        assertNotNull(result);
        assertEquals(entityUser.getId(), result.getId());
        assertEquals(entityUser.getUsername(), result.getUsername());
        assertEquals(entityUser.getEmail(), result.getEmail());
        assertEquals(entityUser.getStatus(), result.getStatus());
        
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
    
    @Test
    @DisplayName("Deve testar UserJpaMapper.toDomain com null")
    void shouldTestUserJpaMapperToDomainWithNull() {
        // Given
        when(userJpaMapper.toDomain(null)).thenReturn(null);
        
        // When
        User result = userJpaMapper.toDomain(null);
        
        // Then
        assertNull(result);
        verify(userJpaMapper, times(1)).toDomain(null);
    }
    
    @Test
    @DisplayName("Deve testar UserJpaMapper com conversão bidirecional")
    void shouldTestUserJpaMapperWithBidirectionalConversion() {
        // Given
        User originalUser = UserTestBuilder.aUser()
            .withId(userId)
            .withUsername("testuser")
            .withEmail("test@example.com")
            .withPasswordHash("hashedpassword")
            .asActive()
            .buildDomain();
        
        when(userJpaMapper.toJpaEntity(originalUser)).thenReturn(userJpaEntity);
        when(userJpaMapper.toDomain(userJpaEntity)).thenReturn(originalUser);
        
        // When
        UserJpaEntity entityUser = userJpaMapper.toJpaEntity(originalUser);
        User domainUser = userJpaMapper.toDomain(entityUser);
        
        // Then
        assertNotNull(entityUser);
        assertNotNull(domainUser);
        assertEquals(originalUser.getId(), entityUser.getId());
        assertEquals(originalUser.getId(), domainUser.getId());
        assertEquals(originalUser.getUsername(), entityUser.getUsername());
        assertEquals(originalUser.getUsername(), domainUser.getUsername());
        assertEquals(originalUser.getEmail(), entityUser.getEmail());
        assertEquals(originalUser.getEmail(), domainUser.getEmail());
        assertEquals(originalUser.getPasswordHash(), entityUser.getPasswordHash());
        assertEquals(originalUser.getPasswordHash(), domainUser.getPasswordHash());
        
        verify(userJpaMapper, times(1)).toJpaEntity(originalUser);
        verify(userJpaMapper, times(1)).toDomain(userJpaEntity);
    }
}
