package com.keepguard.ms_auth.domain.dto.authority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthoritySearchQueryDTO {

    private String name;
    private String description;
    private Pageable pageable;
}

