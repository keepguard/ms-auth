package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.mapper.RoleApplicationMapper;
import com.keepguard.ms_auth.application.port.in.RolePort;
import com.keepguard.ms_auth.domain.dto.role.*;
import com.keepguard.ms_auth.domain.entity.role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleUseCaseService implements RolePort {

    private final RoleCommandService roleCommandService;
    private final RoleQueryService roleQueryService;
    private final RoleApplicationMapper roleMapper;

    @Override
    public RoleCreateView create(RoleCreateCommandDTO command) {
        log.info("Creating role: {}", command.getName());
        return roleCommandService.create(command);
    }

    @Override
    public RoleUpdateView update(RoleUpdateCommandDTO command) {
        log.info("Updating role with ID: {}", command.getId());
        return roleCommandService.update(command);
    }

    @Override
    public void delete(RoleDeleteCommandDTO command) {
        log.info("Deleting role with ID: {}", command.getId());
        roleCommandService.delete(command);
    }

    @Override
    public Optional<RoleGetByIdView> findById(RoleGetByIdQueryDTO command) {
        log.debug("Finding role by ID: {}", command.getId());
        return roleQueryService.findById(command.getId())
                .map(roleMapper::toGetByIdView);
    }

    @Override
    public Optional<RoleGetByNameView> findByName(RoleGetByNameQueryDTO command) {
        log.debug("Finding role by name: {}", command.getName());
        return roleQueryService.findByName(command.getName())
                .map(roleMapper::toGetByNameView);
    }

    @Override
    public List<RoleListView> findAll(RoleGetAllQueryDTO command) {
        log.debug("Finding all roles");
        return roleQueryService.findAll().stream()
                .map(roleMapper::toListView)
                .toList();
    }

    @Override
    public PageResultView<RoleSearchView> findAll(RoleSearchQueryDTO command) {
        log.debug("Finding all roles with pagination");
        PageResultView<Role> pageResultView = roleQueryService.findAll(command.getPageable());

        List<RoleSearchView> content = pageResultView.getContent().stream()
                .map(roleMapper::toSearchView)
                .toList();

        return PageResultView.<RoleSearchView>builder()
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
    public RoleAddAuthorityView addAuthority(RoleAddAuthorityCommandDTO command) {
        log.info("Adding authority {} to role: {}", command.getAuthorityName(), command.getRoleId());
        return roleCommandService.addAuthority(command);
    }

    @Override
    public RoleRemoveAuthorityView removeAuthority(RoleRemoveAuthorityCommandDTO command) {
        log.info("Removing authority {} from role: {}", command.getAuthorityName(), command.getRoleId());
        return roleCommandService.removeAuthority(command);
    }
}
