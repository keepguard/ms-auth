package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.dto.role.RoleCreateView;
import com.keepguard.ms_auth.application.dto.role.RoleUpdateView;
import com.keepguard.ms_auth.application.mapper.RoleApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.AlreadyExistsException;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.dto.role.*;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RoleCommandService
 * Inclui verificações de métricas usando o serviço genérico MetricsService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Command Service Tests")
class RoleCommandServiceTest {
    
    @Mock
    private RoleRepositoryPort roleRepository;
    
    @Mock
    private MetricsPort metricsPort;
    
    @Mock
    private RoleApplicationMapper roleApplicationMapper;
    
    @InjectMocks
    private RoleCommandService roleCommandService;
    
    private Role role;
    private UUID roleId;
    private RoleCreateCommandDTO createCommand;
    private RoleUpdateCommandDTO updateCommand;
    private RoleDeleteCommandDTO deleteCommand;
    
    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        
        // Criar role de teste usando builder
        role = RoleTestBuilder.builder()
            .withId(roleId)
            .buildDomain();
            
        // Criar CommandDTOs de teste
        createCommand = RoleTestBuilder.builder()
            .withName("ADMIN")
            .withDescription("Administrator role")
            .buildCreateCommand();
            
        updateCommand = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN_UPDATED")
            .withDescription("Updated administrator role")
            .buildUpdateCommand();
            
        deleteCommand = RoleTestBuilder.builder()
            .withId(roleId)
            .buildDeleteCommand();
    }
    
    @Test
    @DisplayName("Deve criar role com sucesso")
    void shouldCreateRoleSuccessfully() {
        // Given
        when(roleRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleApplicationMapper.toCreateView(role)).thenReturn(new RoleCreateView(roleId, "ADMIN", "Admin role", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleCreateView result = roleCommandService.create(createCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("ADMIN", result.name());
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
        
        verify(roleRepository).findByName(createCommand.getName());
        verify(roleRepository).save(any(Role.class));
        verify(metricsPort).incrementCounter(eq("role_created_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao criar role com nome duplicado")
    void shouldThrowExceptionWhenCreatingRoleWithDuplicateName() {
        // Given
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));
        
        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            roleCommandService.create(createCommand);
        });
        
        assertEquals("Role name already exists: " + createCommand.getName(), exception.getMessage());
        
        verify(roleRepository).findByName(createCommand.getName());
        verify(roleRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("role_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve atualizar role com sucesso")
    void shouldUpdateRoleSuccessfully() {
        // Given
        Role existingRole = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN_ORIGINAL")
            .buildDomain();
        
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.findByName(updateCommand.getName())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role savedRole = invocation.getArgument(0);
            savedRole.setId(roleId);
            savedRole.setName(updateCommand.getName());
            savedRole.setDescription(updateCommand.getDescription());
            return savedRole;
        });
        when(roleApplicationMapper.toUpdateView(any(Role.class))).thenReturn(new RoleUpdateView(roleId, "ADMIN_UPDATED", "Updated description", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleUpdateView result = roleCommandService.update(updateCommand);
        
        // Then
        assertNotNull(result);
        assertEquals(roleId, result.id());
        assertEquals("ADMIN_UPDATED", result.name());
        assertNotNull(result.updatedAt());
        
        verify(roleRepository).findById(roleId);
        verify(roleRepository).findByName(updateCommand.getName());
        verify(roleRepository).save(any(Role.class));
        verify(metricsPort).incrementCounter(eq("role_updated_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao atualizar role inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentRole() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roleCommandService.update(updateCommand);
        });
        
        assertEquals("Role not found with ID: " + roleId, exception.getMessage());
        
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).findByName(anyString());
        verify(roleRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("role_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao atualizar role com nome duplicado")
    void shouldThrowExceptionWhenUpdatingRoleWithDuplicateName() {
        // Given
        Role existingRole = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN_ORIGINAL")
            .buildDomain();
            
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.findByName(updateCommand.getName())).thenReturn(Optional.of(role));
        
        // When & Then
        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> {
            roleCommandService.update(updateCommand);
        });
        
        assertEquals("Role name already exists: " + updateCommand.getName(), exception.getMessage());
        
        verify(roleRepository).findById(roleId);
        verify(roleRepository).findByName(updateCommand.getName());
        verify(roleRepository, never()).save(any());
        verify(metricsPort).incrementCounter(eq("role_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve deletar role com sucesso")
    void shouldDeleteRoleSuccessfully() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        
        // When
        assertDoesNotThrow(() -> {
            roleCommandService.delete(deleteCommand);
        });
        
        // Then
        verify(roleRepository).findById(roleId);
        verify(roleRepository).delete(role);
        verify(metricsPort).incrementCounter(eq("role_deleted_total"), any());
    }
    
    @Test
    @DisplayName("Deve lançar exceção ao deletar role inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentRole() {
        // Given
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());
        
        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roleCommandService.delete(deleteCommand);
        });
        
        assertEquals("Role not found with ID: " + roleId, exception.getMessage());
        
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).delete(any());
        verify(metricsPort).incrementCounter(eq("role_business_errors_total"), any());
    }
    
    @Test
    @DisplayName("Deve permitir atualização quando nome não mudou")
    void shouldAllowUpdateWhenNameNotChanged() {
        // Given
        Role existingRole = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN")
            .buildDomain();
            
        RoleUpdateCommandDTO sameNameCommand = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN") // Mesmo nome
            .withDescription("Updated description")
            .buildUpdateCommand();
            
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleApplicationMapper.toUpdateView(any(Role.class))).thenReturn(new RoleUpdateView(roleId, "ADMIN", "Updated description", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleUpdateView result = roleCommandService.update(sameNameCommand);
        
        // Then
        assertNotNull(result);
        verify(roleRepository).findById(roleId);
        verify(roleRepository, never()).findByName(anyString()); // Não deve verificar nome duplicado
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    @DisplayName("Deve criar role com nome em maiúsculas")
    void shouldCreateRoleWithUppercaseName() {
        // Given
        RoleCreateCommandDTO uppercaseCommand = RoleTestBuilder.builder()
            .withName("ADMIN")
            .withDescription("Administrator role")
            .buildCreateCommand();
            
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleApplicationMapper.toCreateView(role)).thenReturn(new RoleCreateView(roleId, "ADMIN", "Administrator role", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleCreateView result = roleCommandService.create(uppercaseCommand);
        
        // Then
        assertNotNull(result);
        verify(roleRepository).findByName("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    @DisplayName("Deve atualizar role com nome em maiúsculas")
    void shouldUpdateRoleWithUppercaseName() {
        // Given
        Role existingRole = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("admin")
            .buildDomain();
            
        RoleUpdateCommandDTO uppercaseCommand = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN")
            .withDescription("Updated description")
            .buildUpdateCommand();
            
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleApplicationMapper.toUpdateView(any(Role.class))).thenReturn(new RoleUpdateView(roleId, "ADMIN", "Updated description", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleUpdateView result = roleCommandService.update(uppercaseCommand);
        
        // Then
        assertNotNull(result);
        verify(roleRepository).findById(roleId);
        verify(roleRepository).findByName("ADMIN");
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    @DisplayName("Deve atualizar campo updatedAt ao atualizar role")
    void shouldUpdateUpdatedAtFieldWhenUpdatingRole() {
        // Given
        LocalDateTime originalUpdatedAt = LocalDateTime.now().minusDays(1);
        Role existingRole = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN_ORIGINAL")
            .withUpdatedAt(originalUpdatedAt)
            .buildDomain();
            
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(existingRole));
        when(roleRepository.findByName(updateCommand.getName())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role savedRole = invocation.getArgument(0);
            savedRole.setUpdatedAt(LocalDateTime.now());
            return savedRole;
        });
        when(roleApplicationMapper.toUpdateView(any(Role.class))).thenReturn(new RoleUpdateView(roleId, "ADMIN_UPDATED", "Updated description", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleUpdateView result = roleCommandService.update(updateCommand);
        
        // Then
        assertNotNull(result);
        assertTrue(result.updatedAt().isAfter(originalUpdatedAt));
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    @DisplayName("Deve definir campos de auditoria ao criar role")
    void shouldSetAuditFieldsWhenCreatingRole() {
        // Given
        when(roleRepository.findByName(createCommand.getName())).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role savedRole = invocation.getArgument(0);
            savedRole.setId(roleId);
            savedRole.setCreatedAt(LocalDateTime.now());
            savedRole.setUpdatedAt(LocalDateTime.now());
            return savedRole;
        });
        when(roleApplicationMapper.toCreateView(any(Role.class))).thenReturn(new RoleCreateView(roleId, "ADMIN", "Admin role", LocalDateTime.now(), LocalDateTime.now()));
        
        // When
        RoleCreateView result = roleCommandService.create(createCommand);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.createdAt());
        assertNotNull(result.updatedAt());
        verify(roleRepository).save(any(Role.class));
    }
    
    @Test
    @DisplayName("Deve testar entidade Role com construtor padrão")
    void shouldTestRoleEntityWithDefaultConstructor() {
        // Given & When
        Role role = new Role();
        
        // Then
        assertNotNull(role);
        assertNull(role.getId());
        assertNull(role.getName());
        assertNull(role.getDescription());
        assertNull(role.getCreatedAt());
        assertNull(role.getUpdatedAt());
        // Role entity doesn't have active field
    }
    
    @Test
    @DisplayName("Deve testar entidade Role com todos os construtores")
    void shouldTestRoleEntityWithAllConstructors() {
        // Given
        UUID id = UUID.randomUUID();
        String name = "TEST_ROLE";
        String description = "Test role description";
        LocalDateTime now = LocalDateTime.now();
        
        // When
        Role role = Role.builder()
            .id(id)
            .name(name)
            .description(description)
            .createdAt(now)
            .updatedAt(now)
            .build();
        
        // Then
        assertNotNull(role);
        assertEquals(id, role.getId());
        assertEquals(name, role.getName());
        assertEquals(description, role.getDescription());
        assertEquals(now, role.getCreatedAt());
        assertEquals(now, role.getUpdatedAt());
        // Role entity doesn't have active field
    }
    
    @Test
    @DisplayName("Deve testar entidade Role com dados válidos")
    void shouldTestRoleEntityWithValidData() {
        // Given
        Role role = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN")
            .withDescription("Administrator role")
            .withActive(true)
            .buildDomain();
        
        // When & Then
        assertNotNull(role);
        assertEquals(roleId, role.getId());
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
        // Role entity doesn't have active field
        assertNotNull(role.getCreatedAt());
        assertNotNull(role.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve testar método preUpdate da entidade Role")
    void shouldTestRoleEntityPreUpdateMethod() {
        // Given
        Role role = RoleTestBuilder.builder()
            .withId(roleId)
            .withName("ADMIN")
            .withDescription("Administrator role")
            .buildDomain();
            
        LocalDateTime originalUpdatedAt = role.getUpdatedAt();
        
        // When
        role.updateTimestamp();
        
        // Then
        assertTrue(role.getUpdatedAt().isAfter(originalUpdatedAt) || 
                  role.getUpdatedAt().isEqual(originalUpdatedAt));
    }
}