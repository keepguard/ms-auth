package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.dto.authority.*;

import java.util.List;
import java.util.Optional;

public interface AuthorityPort {

    AuthorityCreateViewDTO create(AuthorityCreateCommandDTO command);

    AuthorityUpdateViewDTO update(AuthorityUpdateCommandDTO command);

    void delete(AuthorityDeleteCommandDTO command);

    Optional<AuthorityGetByIdViewDTO> findById(AuthorityGetByIdQueryDTO command);

    Optional<AuthorityGetByNameViewDTO> findByName(AuthorityGetByNameQueryDTO command);

    List<AuthorityListViewDTO> findAll(AuthorityGetAllQueryDTO command);

    PageResultViewDTO<AuthoritySearchViewDTO> findAll(AuthoritySearchQueryDTO command);
}

