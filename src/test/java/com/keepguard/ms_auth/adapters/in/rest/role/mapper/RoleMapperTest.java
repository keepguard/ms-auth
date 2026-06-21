package com.keepguard.ms_auth.adapters.in.rest.role.mapper;

import com.keepguard.ms_auth.adapters.in.rest.role.dto.RoleCreateDTO;
import com.keepguard.ms_auth.adapters.in.rest.role.dto.RoleResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.role.dto.RoleUpdateDTO;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para RoleMapper
 * Cobertura: 100% dos métodos públicos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Role Mapper Tests")
class RoleMapperTest {
    
    @InjectMocks
    private RoleAdapterMapper roleAdapterMapper;
    
    private UUID roleId;
    private Role role;
    private RoleCreateDTO createDTO;
    private RoleUpdateDTO updateDTO;
    private RoleResponseDTO responseDTO;
    
    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        
        // Criar objetos de teste usando builder
        role = RoleTestBuilder.builder()
            .withId(roleId)
            .buildDomain();
        
        createDTO = RoleTestBuilder.builder()
            .withId(roleId)
            .buildCreateDTO();
        
        updateDTO = RoleTestBuilder.builder()
            .withId(roleId)
            .buildUpdateDTO();
        
        responseDTO = RoleTestBuilder.builder()
            .withId(roleId)
            .buildResponseDTO();
    }
    
    @Test
    @DisplayName("Deve converter RoleCreateDTO para Role com sucesso")
    void shouldConvertRoleCreateDTOToRoleSuccessfully() {
        // When
        Role result = roleAdapterMapper.toEntity(createDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(createDTO.getName(), result.getName());
        assertEquals(createDTO.getDescription(), result.getDescription());
        assertNull(result.getId()); // ID não é definido no DTO de criação
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter RoleCreateDTO nulo para Role nulo")
    void shouldConvertNullRoleCreateDTOToNullRole() {
        // When
        Role result = roleAdapterMapper.toEntity((RoleCreateDTO) null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve converter RoleCreateDTO com campos nulos")
    void shouldConvertRoleCreateDTOWithNullFields() {
        // Given
        RoleCreateDTO createDTOWithNulls = RoleCreateDTO.builder()
            .name(null)
            .description(null)
            .build();
        
        // When
        Role result = roleAdapterMapper.toEntity(createDTOWithNulls);
        
        // Then
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter RoleUpdateDTO para Role com sucesso")
    void shouldConvertRoleUpdateDTOToRoleSuccessfully() {
        // When
        Role result = roleAdapterMapper.toEntity(updateDTO);
        
        // Then
        assertNotNull(result);
        assertEquals(updateDTO.getName(), result.getName());
        assertEquals(updateDTO.getDescription(), result.getDescription());
        assertNull(result.getId()); // ID não é definido no DTO de atualização
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter RoleUpdateDTO nulo para Role nulo")
    void shouldConvertNullRoleUpdateDTOToNullRole() {
        // When
        Role result = roleAdapterMapper.toEntity((RoleUpdateDTO) null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve converter RoleUpdateDTO com campos nulos")
    void shouldConvertRoleUpdateDTOWithNullFields() {
        // Given
        RoleUpdateDTO updateDTOWithNulls = RoleUpdateDTO.builder()
            .name(null)
            .description(null)
            .build();
        
        // When
        Role result = roleAdapterMapper.toEntity(updateDTOWithNulls);
        
        // Then
        assertNotNull(result);
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter Role para RoleResponseDTO com sucesso")
    void shouldConvertRoleToRoleResponseDTOSuccessfully() {
        // When
        RoleResponseDTO result = roleAdapterMapper.toResponseDTO(role);
        
        // Then
        assertNotNull(result);
        assertEquals(role.getId(), result.getId());
        assertEquals(role.getName(), result.getName());
        assertEquals(role.getDescription(), result.getDescription());
        assertEquals(role.getCreatedAt(), result.getCreatedAt());
        assertEquals(role.getUpdatedAt(), result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter Role nulo para RoleResponseDTO nulo")
    void shouldConvertNullRoleToNullRoleResponseDTO() {
        // When
        RoleResponseDTO result = roleAdapterMapper.toResponseDTO((Role) null);
        
        // Then
        assertNull(result);
    }
    
    @Test
    @DisplayName("Deve converter Role com campos nulos")
    void shouldConvertRoleWithNullFields() {
        // Given
        Role roleWithNulls = Role.builder()
            .id(null)
            .name(null)
            .description(null)
            .createdAt(null)
            .updatedAt(null)
            .build();
        
        // When
        RoleResponseDTO result = roleAdapterMapper.toResponseDTO(roleWithNulls);
        
        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter Role com valores específicos")
    void shouldConvertRoleWithSpecificValues() {
        // Given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 11, 30, 0);
        
        Role specificRole = Role.builder()
            .id(roleId)
            .name("USER")
            .description("Usuário comum do sistema")
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
        
        // When
        RoleResponseDTO result = roleAdapterMapper.toResponseDTO(specificRole);
        
        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("USER", result.getName());
        assertEquals("Usuário comum do sistema", result.getDescription());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(updatedAt, result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter RoleCreateDTO com valores específicos")
    void shouldConvertRoleCreateDTOWithSpecificValues() {
        // Given
        RoleCreateDTO specificCreateDTO = RoleCreateDTO.builder()
            .name("MANAGER")
            .description("Gerente do sistema")
            .build();
        
        // When
        Role result = roleAdapterMapper.toEntity(specificCreateDTO);
        
        // Then
        assertNotNull(result);
        assertEquals("MANAGER", result.getName());
        assertEquals("Gerente do sistema", result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter RoleUpdateDTO com valores específicos")
    void shouldConvertRoleUpdateDTOWithSpecificValues() {
        // Given
        RoleUpdateDTO specificUpdateDTO = RoleUpdateDTO.builder()
            .name("ADMIN_UPDATED")
            .description("Administrador atualizado")
            .build();
        
        // When
        Role result = roleAdapterMapper.toEntity(specificUpdateDTO);
        
        // Then
        assertNotNull(result);
        assertEquals("ADMIN_UPDATED", result.getName());
        assertEquals("Administrador atualizado", result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter Role com descrição vazia")
    void shouldConvertRoleWithEmptyDescription() {
        // Given
        Role roleWithEmptyDescription = Role.builder()
            .id(roleId)
            .name("GUEST")
            .description("")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When
        RoleResponseDTO result = roleAdapterMapper.toResponseDTO(roleWithEmptyDescription);
        
        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("GUEST", result.getName());
        assertEquals("", result.getDescription());
    }
    
    @Test
    @DisplayName("Deve converter RoleCreateDTO com descrição vazia")
    void shouldConvertRoleCreateDTOWithEmptyDescription() {
        // Given
        RoleCreateDTO createDTOWithEmptyDescription = RoleCreateDTO.builder()
            .name("GUEST")
            .description("")
            .build();
        
        // When
        Role result = roleAdapterMapper.toEntity(createDTOWithEmptyDescription);
        
        // Then
        assertNotNull(result);
        assertEquals("GUEST", result.getName());
        assertEquals("", result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter Role com nome em maiúsculo")
    void shouldConvertRoleWithUppercaseName() {
        // Given
        Role roleWithUppercase = Role.builder()
            .id(roleId)
            .name("ADMIN")
            .description("Administrador do sistema")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        // When
        RoleResponseDTO result = roleAdapterMapper.toResponseDTO(roleWithUppercase);
        
        // Then
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("ADMIN", result.getName());
        assertEquals("Administrador do sistema", result.getDescription());
    }
    
    @Test
    @DisplayName("Deve converter RoleCreateDTO com nome em maiúsculo")
    void shouldConvertRoleCreateDTOWithUppercaseName() {
        // Given
        RoleCreateDTO createDTOWithUppercase = RoleCreateDTO.builder()
            .name("USER")
            .description("Usuário comum")
            .build();
        
        // When
        Role result = roleAdapterMapper.toEntity(createDTOWithUppercase);
        
        // Then
        assertNotNull(result);
        assertEquals("USER", result.getName());
        assertEquals("Usuário comum", result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
    
    @Test
    @DisplayName("Deve converter RoleUpdateDTO com nome em maiúsculo")
    void shouldConvertRoleUpdateDTOWithUppercaseName() {
        // Given
        RoleUpdateDTO updateDTOWithUppercase = RoleUpdateDTO.builder()
            .name("MANAGER")
            .description("Gerente do sistema")
            .build();
        
        // When
        Role result = roleAdapterMapper.toEntity(updateDTOWithUppercase);
        
        // Then
        assertNotNull(result);
        assertEquals("MANAGER", result.getName());
        assertEquals("Gerente do sistema", result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }
}
