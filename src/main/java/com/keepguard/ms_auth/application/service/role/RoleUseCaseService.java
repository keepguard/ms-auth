package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.mapper.RoleApplicationMapper;
import com.keepguard.ms_auth.application.port.in.RolePort;
import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.domain.entity.role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleUseCaseService implements RolePort {

    private final RoleCommandService roleCommandService;
    private final RoleQueryService roleQueryService;
    private final RoleApplicationMapper roleMapper;

    @Override
    public RoleCreateViewDTO create(RoleCreateCommandDTO command) {
        log.info("Creating role: {}", command.getName());
        return roleCommandService.create(command);
    }

    @Override
    public RoleUpdateViewDTO update(RoleUpdateCommandDTO command) {
        log.info("Updating role with ID: {}", command.getId());
        return roleCommandService.update(command);
    }

    @Override
    public void delete(RoleDeleteCommandDTO command) {
        log.info("Deleting role with ID: {}", command.getId());
        roleCommandService.delete(command);
    }

    @Override
    public Optional<RoleGetByIdViewDTO> findById(RoleGetByIdQueryDTO command) {
        log.debug("Finding role by ID: {}", command.getId());
        UUID companyId = command.getCompanyId();
        return roleQueryService.findByIdForCompany(command.getId(), companyId)
                .map(roleMapper::toGetByIdView);
    }

    @Override
    public Optional<RoleGetByNameViewDTO> findByName(RoleGetByNameQueryDTO command) {
        log.debug("Finding role by name: {}", command.getName());
        UUID companyId = command.getCompanyId();
        return roleQueryService.findByCompanyIdAndName(companyId, command.getName())
                .map(roleMapper::toGetByNameView);
    }

    @Override
    public List<RoleListViewDTO> findAll(RoleGetAllQueryDTO command) {
        log.debug("Finding all roles");
        UUID companyId = command.getCompanyId();
        return roleQueryService.findByCompanyId(companyId).stream()
                .map(roleMapper::toListView)
                .toList();
    }

    @Override
    public PageResultViewDTO<RoleSearchViewDTO> findAll(RoleSearchQueryDTO command) {
        log.debug("Finding all roles with pagination");
        UUID companyId = command.getCompanyId();
        PageResultViewDTO<Role> pageResultView = roleQueryService.findByCompanyId(companyId, command.getPageable());

        List<RoleSearchViewDTO> content = pageResultView.getContent().stream()
                .map(roleMapper::toSearchView)
                .toList();

        return PageResultViewDTO.<RoleSearchViewDTO>builder()
                .content(content)
                .totalElements(pageResultView.getTotalElements())
                .totalPages(pageResultView.getTotalPages())
                .size(pageResultView.getSize())
                .pageNumber(pageResultView.getPageNumber())
                .first(pageResultView.isFirst())
                .last(pageResultView.isLast())
                .numberOfElements(pageResultView.getNumberOfElements())
                .build();
    }

    @Override
    public RoleAddAuthorityViewDTO addAuthority(RoleAddAuthorityCommandDTO command) {
        log.info("Adding authority {} to role: {}", command.getAuthorityName(), command.getRoleId());
        return roleCommandService.addAuthority(command);
    }

    @Override
    public RoleRemoveAuthorityViewDTO removeAuthority(RoleRemoveAuthorityCommandDTO command) {
        log.info("Removing authority {} from role: {}", command.getAuthorityName(), command.getRoleId());
        return roleCommandService.removeAuthority(command);
    }
}
