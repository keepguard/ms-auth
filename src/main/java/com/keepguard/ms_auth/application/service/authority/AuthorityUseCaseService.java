package com.keepguard.ms_auth.application.service.authority;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.port.in.AuthorityPort;
import com.keepguard.ms_auth.domain.dto.authority.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorityUseCaseService implements AuthorityPort {

    private final AuthorityCommandService authorityCommandService;
    private final AuthorityQueryService authorityQueryService;

    @Override
    public AuthorityCreateView create(AuthorityCreateCommandDTO command) {
        log.info("Creating authority: {}", command.getName());
        return authorityCommandService.create(command);
    }

    @Override
    public AuthorityUpdateView update(AuthorityUpdateCommandDTO command) {
        log.info("Updating authority with ID: {}", command.getId());
        return authorityCommandService.update(command);
    }

    @Override
    public void delete(AuthorityDeleteCommandDTO command) {
        log.info("Deleting authority with ID: {}", command.getId());
        authorityCommandService.delete(command);
    }

    @Override
    public Optional<AuthorityGetByIdView> findById(AuthorityGetByIdQueryDTO command) {
        log.debug("Finding authority by ID: {}", command.getId());
        return authorityQueryService.findById(command.getId());
    }

    @Override
    public Optional<AuthorityGetByNameView> findByName(AuthorityGetByNameQueryDTO command) {
        log.debug("Finding authority by name: {}", command.getName());
        return authorityQueryService.findByName(command.getName());
    }

    @Override
    public List<AuthorityListView> findAll(AuthorityGetAllQueryDTO command) {
        log.debug("Finding all authorities");
        return authorityQueryService.findAll();
    }

    @Override
    public PageResultView<AuthoritySearchView> findAll(AuthoritySearchQueryDTO command) {
        log.debug("Finding all authorities with pagination");
        return authorityQueryService.findAll(command.getPageable());
    }
}

