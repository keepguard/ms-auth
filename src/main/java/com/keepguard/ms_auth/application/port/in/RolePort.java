package com.keepguard.ms_auth.application.port.in;

import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.application.dto.common.PageResultViewDTO;
import com.keepguard.ms_auth.application.dto.role.*;

import java.util.List;
import java.util.Optional;

public interface RolePort {

    RoleCreateViewDTO create(RoleCreateCommandDTO command);

    RoleUpdateViewDTO update(RoleUpdateCommandDTO command);

    void delete(RoleDeleteCommandDTO command);

    Optional<RoleGetByIdViewDTO> findById(RoleGetByIdQueryDTO command);

    Optional<RoleGetByNameViewDTO> findByName(RoleGetByNameQueryDTO command);

    List<RoleListViewDTO> findAll(RoleGetAllQueryDTO command);

    PageResultViewDTO<RoleSearchViewDTO> findAll(RoleSearchQueryDTO command);

    RoleAddAuthorityViewDTO addAuthority(RoleAddAuthorityCommandDTO command);

    RoleRemoveAuthorityViewDTO removeAuthority(RoleRemoveAuthorityCommandDTO command);
}
