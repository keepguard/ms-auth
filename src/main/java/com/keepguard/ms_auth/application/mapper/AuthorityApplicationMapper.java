package com.keepguard.ms_auth.application.mapper;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.domain.entity.authority.Authority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthorityApplicationMapper {

    public AuthorityCreateView toCreateView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityCreateView(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityUpdateView toUpdateView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityUpdateView(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityGetByIdView toGetByIdView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityGetByIdView(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityGetByNameView toGetByNameView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityGetByNameView(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthorityListView toListView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthorityListView(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }

    public AuthoritySearchView toSearchView(Authority authority) {
        if (authority == null) {
            return null;
        }

        return new AuthoritySearchView(
                authority.getId(),
                authority.getName(),
                authority.getDescription(),
                authority.getCreatedAt(),
                authority.getUpdatedAt()
        );
    }
}

