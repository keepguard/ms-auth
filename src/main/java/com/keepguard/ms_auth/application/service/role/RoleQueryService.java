package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.application.service.exception.QueryOperationException;
import com.keepguard.ms_auth.domain.entity.role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleQueryService {

    private final RoleRepositoryPort roleRepository;

    public Optional<Role> findById(UUID id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public PageResultView<Role> findAll(Pageable pageable) {

        Page<Role> page = roleRepository.findAll(pageable);

        return PageResultView.<Role>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .page(page.getNumber())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

    }
}
