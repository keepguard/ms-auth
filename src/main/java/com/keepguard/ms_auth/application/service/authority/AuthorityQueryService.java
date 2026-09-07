package com.keepguard.ms_auth.application.service.authority;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.mapper.AuthorityApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.AuthorityRepositoryPort;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
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
public class AuthorityQueryService {

    private final AuthorityRepositoryPort authorityRepository;
    private final AuthorityApplicationMapper authorityMapper;

    public Optional<AuthorityGetByIdViewDTO> findById(UUID id) {
        return authorityRepository.findById(id)
                .map(authorityMapper::toGetByIdView);
    }

    public Optional<AuthorityGetByNameViewDTO> findByName(String name) {
        return authorityRepository.findByName(name)
                .map(authorityMapper::toGetByNameView);
    }

    public List<AuthorityListViewDTO> findAll() {
        return authorityRepository.findAll().stream()
                .map(authorityMapper::toListView)
                .toList();
    }

    public PageResultViewDTO<AuthoritySearchViewDTO> findAll(Pageable pageable) {
        return toPageResult(authorityRepository.findAll(pageable));
    }

    private PageResultViewDTO<AuthoritySearchViewDTO> toPageResult(Page<Authority> page) {
        List<AuthoritySearchViewDTO> content = page.getContent().stream()
                .map(authorityMapper::toSearchView)
                .toList();

        return PageResultViewDTO.<AuthoritySearchViewDTO>builder()
                .content(content)
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
