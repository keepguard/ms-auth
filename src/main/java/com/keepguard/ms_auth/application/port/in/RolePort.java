package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.dto.role.*;

import java.util.List;
import java.util.Optional;

public interface RolePort {

    RoleCreateView create(RoleCreateCommandDTO command);

    RoleUpdateView update(RoleUpdateCommandDTO command);

    void delete(RoleDeleteCommandDTO command);

    Optional<RoleGetByIdView> findById(RoleGetByIdQueryDTO command);

    Optional<RoleGetByNameView> findByName(RoleGetByNameQueryDTO command);

    List<RoleListView> findAll(RoleGetAllQueryDTO command);

    PageResultView<RoleSearchView> findAll(RoleSearchQueryDTO command);

    RoleAddAuthorityView addAuthority(RoleAddAuthorityCommandDTO command);

    RoleRemoveAuthorityView removeAuthority(RoleRemoveAuthorityCommandDTO command);
}
