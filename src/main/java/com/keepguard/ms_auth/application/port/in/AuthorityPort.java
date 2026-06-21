package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.dto.authority.*;

import java.util.List;
import java.util.Optional;

public interface AuthorityPort {

    AuthorityCreateView create(AuthorityCreateCommandDTO command);

    AuthorityUpdateView update(AuthorityUpdateCommandDTO command);

    void delete(AuthorityDeleteCommandDTO command);

    Optional<AuthorityGetByIdView> findById(AuthorityGetByIdQueryDTO command);

    Optional<AuthorityGetByNameView> findByName(AuthorityGetByNameQueryDTO command);

    List<AuthorityListView> findAll(AuthorityGetAllQueryDTO command);

    PageResultView<AuthoritySearchView> findAll(AuthoritySearchQueryDTO command);
}

