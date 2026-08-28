package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.application.dto.role.ProvisionCompanyRolesView;
import com.keepguard.ms_auth.application.port.out.metrics.MetricsPort;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.CompanyRoleRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.NotFoundException;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import com.keepguard.ms_auth.domain.entity.authority.SystemAuthorityNames;
import com.keepguard.ms_auth.domain.entity.role.CompanyRole;
import com.keepguard.ms_auth.domain.entity.role.Role;
import com.keepguard.ms_auth.domain.entity.role.SystemRoleNames;
import com.keepguard.ms_auth.test.builder.RoleTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Company Role Provision Service Tests")
class CompanyRoleProvisionServiceTest {

    @Mock
    private RoleRepositoryPort roleRepository;
    @Mock
    private CompanyRoleRepositoryPort companyRoleRepository;
    @Mock
    private AuthorityRepositoryPort authorityRepository;
    @Mock
    private MetricsPort metricsPort;

    @InjectMocks
    private CompanyRoleProvisionService service;

    @Test
    @DisplayName("Deve ser idempotente quando company já foi provisionada")
    void shouldReturnExistingWhenAlreadyProvisioned() {
        UUID companyId = UUID.randomUUID();
        Role existing = RoleTestBuilder.builder()
            .withCompanyId(companyId)
            .withName(SystemRoleNames.ROLE_USER)
            .buildDomain();
        when(companyRoleRepository.existsByCompanyId(companyId)).thenReturn(true);
        when(roleRepository.findByCompanyId(companyId)).thenReturn(List.of(existing));

        ProvisionCompanyRolesView view = service.provision(companyId);

        assertTrue(view.alreadyProvisioned());
        assertEquals(List.of(SystemRoleNames.ROLE_USER), view.roleNames());
        verify(roleRepository, never()).save(any());
        verify(companyRoleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve clonar templates ADMIN, MANAGER e USER para a company")
    void shouldCloneTemplatesForCompany() {
        UUID companyId = UUID.randomUUID();
        UUID companyB = UUID.randomUUID();
        when(companyRoleRepository.existsByCompanyId(companyId)).thenReturn(false);

        for (String name : SystemRoleNames.PROVISIONED) {
            Role template = RoleTestBuilder.builder()
                .withCompanyId(null)
                .withName(name)
                .withSystem(true)
                .buildDomain();
            when(roleRepository.findByCompanyIdIsNullAndName(name)).thenReturn(Optional.of(template));
        }
        for (String name : SystemAuthorityNames.TEMPLATES) {
            Authority template = Authority.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("template " + name)
                .companyId(null)
                .build();
            when(authorityRepository.findByCompanyIdIsNullAndName(name)).thenReturn(Optional.of(template));
        }

        when(authorityRepository.save(any(Authority.class))).thenAnswer(invocation -> {
            Authority saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        });
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(companyRoleRepository.save(any(CompanyRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProvisionCompanyRolesView view = service.provision(companyId);

        assertFalse(view.alreadyProvisioned());
        assertEquals(3, view.roleNames().size());
        assertTrue(view.roleNames().containsAll(SystemRoleNames.PROVISIONED));
        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(3)).save(roleCaptor.capture());
        Role adminClone = roleCaptor.getAllValues().stream()
            .filter(role -> SystemRoleNames.ROLE_ADMIN.equals(role.getName()))
            .findFirst().orElseThrow();
        Role managerClone = roleCaptor.getAllValues().stream()
            .filter(role -> SystemRoleNames.ROLE_MANAGER.equals(role.getName()))
            .findFirst().orElseThrow();
        Role userClone = roleCaptor.getAllValues().stream()
            .filter(role -> SystemRoleNames.ROLE_USER.equals(role.getName()))
            .findFirst().orElseThrow();
        assertEquals(6, adminClone.getAuthorities().size());
        assertEquals(3, managerClone.getAuthorities().size());
        assertTrue(managerClone.getAuthorities().stream().allMatch(a -> a.getName().startsWith("user:")));
        assertTrue(userClone.getAuthorities().isEmpty());
        verify(authorityRepository, times(6)).save(any(Authority.class));
        ArgumentCaptor<CompanyRole> companyRoleCaptor = ArgumentCaptor.forClass(CompanyRole.class);
        verify(companyRoleRepository, times(3)).save(companyRoleCaptor.capture());
        assertTrue(companyRoleCaptor.getAllValues().stream().anyMatch(CompanyRole::isDefaultRole));
        assertEquals(1, companyRoleCaptor.getAllValues().stream().filter(CompanyRole::isDefaultRole).count());
        verify(roleRepository, never()).findByCompanyIdIsNullAndName(SystemRoleNames.ROLE_SYSTEM);

        when(companyRoleRepository.existsByCompanyId(companyB)).thenReturn(false);
        ProvisionCompanyRolesView viewB = service.provision(companyB);
        assertEquals(3, viewB.roleNames().size());
        verify(roleRepository, times(6)).save(any(Role.class));
    }

    @Test
    @DisplayName("Deve falhar quando template não existe")
    void shouldFailWhenTemplateMissing() {
        UUID companyId = UUID.randomUUID();
        when(companyRoleRepository.existsByCompanyId(companyId)).thenReturn(false);
        when(authorityRepository.findByCompanyIdIsNullAndName(any())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.provision(companyId));
    }
}
