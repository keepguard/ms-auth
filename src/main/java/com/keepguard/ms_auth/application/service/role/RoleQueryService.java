package com.keepguard.ms_auth.application.service.role;

import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.domain.entity.role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Optional<Role> findByIdForCompany(UUID id, UUID companyId) {
        return roleRepository.findById(id)
                .filter(role -> role.belongsToCompany(companyId));
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public Optional<Role> findByCompanyIdAndName(UUID companyId, String name) {
        return roleRepository.findByCompanyIdAndName(companyId, name);
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public List<Role> findByCompanyId(UUID companyId) {
        return roleRepository.findByCompanyId(companyId);
    }

    public PageResultView<Role> findAll(Pageable pageable) {
        return toPageResult(roleRepository.findAll(pageable));
    }

    public PageResultView<Role> findByCompanyId(UUID companyId, Pageable pageable) {
        return toPageResult(roleRepository.findByCompanyId(companyId, pageable));
    }

    private PageResultView<Role> toPageResult(Page<Role> page) {
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
