package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthorityApplicationMapper {

    public AuthorityCreateViewDTO toCreateView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityCreateViewDTO(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityUpdateViewDTO toUpdateView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityUpdateViewDTO(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityGetByIdViewDTO toGetByIdView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityGetByIdViewDTO(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityGetByNameViewDTO toGetByNameView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityGetByNameViewDTO(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityListViewDTO toListView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityListViewDTO(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthoritySearchViewDTO toSearchView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthoritySearchViewDTO(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }
}

