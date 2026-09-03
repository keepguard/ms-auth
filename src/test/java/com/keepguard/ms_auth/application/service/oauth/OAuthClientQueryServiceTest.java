package com.keepguard.ms_auth.application.service.oauth;

import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.mapper.OAuthClientApplicationMapper;
import com.keepguard.ms_auth.application.port.out.persistence.OAuthClientRepositoryPort;
import com.keepguard.ms_auth.application.port.out.persistence.RoleRepositoryPort;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientSearchQueryDTO;
import com.keepguard.ms_auth.domain.entity.oauth.OAuthClient;
import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import com.keepguard.ms_auth.infrastructure.config.security.OAuthClientSecretCrypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("OAuthClientQueryService Tests")
class OAuthClientQueryServiceTest {

    private OAuthClientQueryService queryService;
    private OAuthClientRepositoryPort repository;
    private final UUID companyId = UUID.fromString("f7fc7350-b9fc-4e54-9c58-ac9385b23ae4");

    @BeforeEach
    void setUp() {
        repository = mock(OAuthClientRepositoryPort.class);
        queryService = new OAuthClientQueryService(repository, mock(RoleRepositoryPort.class),
                new OAuthClientApplicationMapper(), new OAuthClientRoleResolver(mock(RoleRepositoryPort.class)),
                new OAuthClientSecretCrypto("test-base"));
    }

    @Test
    @DisplayName("search sem clientId/status envia null ao repositório e pagina createdAt desc")
    void search_withoutOptionalFilters_usesNullsAndDefaultSort() {
        OAuthClient client = sampleClient("investbot-collector");
        when(repository.search(eq(companyId), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(client), PageRequest.of(0, 20), 1));

        PageResultView<OAuthClientView> result = queryService.search(OAuthClientSearchQueryDTO.builder()
                .companyId(companyId)
                .pageable(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")))
                .build());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).search(eq(companyId), isNull(), isNull(), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().iterator().next();
        assertEquals("createdAt", order.getProperty());
        assertEquals(Sort.Direction.DESC, order.getDirection());
        assertEquals(1, result.getTotalElements());
        assertEquals("investbot-collector", result.getContent().get(0).clientId());
    }

    @Test
    @DisplayName("search com clientId em branco trata como sem filtro")
    void search_blankClientId_isTreatedAsAbsent() {
        when(repository.search(eq(companyId), isNull(), eq(OAuthClientStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        queryService.search(OAuthClientSearchQueryDTO.builder()
                .companyId(companyId)
                .clientId("   ")
                .status(OAuthClientStatus.ACTIVE)
                .pageable(PageRequest.of(0, 20))
                .build());

        verify(repository).search(eq(companyId), isNull(), eq(OAuthClientStatus.ACTIVE), any(Pageable.class));
    }

    @Test
    @DisplayName("search ignora propriedade de sort não permitida")
    void search_unknownSort_fallsBackToCreatedAt() {
        when(repository.search(eq(companyId), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        queryService.search(OAuthClientSearchQueryDTO.builder()
                .companyId(companyId)
                .pageable(PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "secretHash")))
                .build());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).search(eq(companyId), isNull(), isNull(), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().iterator().next();
        assertEquals("createdAt", order.getProperty());
        assertEquals(Sort.Direction.ASC, order.getDirection());
        assertTrue(pageableCaptor.getValue().getPageSize() <= 100);
    }

    private OAuthClient sampleClient(String clientId) {
        return OAuthClient.builder()
                .id(UUID.randomUUID())
                .companyId(companyId)
                .clientId(clientId)
                .secretHash("hash")
                .authorities(List.of("knowledge:write"))
                .status(OAuthClientStatus.ACTIVE)
                .tokenTtlSeconds(28800)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
