package com.keepguard.ms_auth.adapters.in.rest.oauth;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientCreateRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientCreateResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.dto.OAuthClientResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.oauth.mapper.OAuthClientAdapterMapper;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.dto.oauth.OAuthClientView;
import com.keepguard.ms_auth.application.port.in.OAuthClientPort;
import com.keepguard.ms_auth.domain.dto.oauth.OAuthClientSearchQueryDTO;
import com.keepguard.ms_auth.domain.enums.OAuthClientStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth/oauth/clients")
@RequiredArgsConstructor
@Tag(name = "OAuth Clients", description = "Cadastro e gestão de clients OAuth de sistema (ADMIN/SYSTEM)")
public class OAuthClientController {

    private final OAuthClientPort oauthClientPort;
    private final OAuthClientAdapterMapper mapper;
    private final OAuthAdminAccess oauthAdminAccess;

    @PostMapping
    @Operation(summary = "Criar OAuth client de sistema")
    @MetricsEndpoint(endpoint = "oauth_client_create", operation = "criar oauth client")
    public ResponseEntity<OAuthClientCreateResponseDTO> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @Valid @RequestBody OAuthClientCreateRequestDTO request) {
        oauthAdminAccess.requireAdminOrSystem(jwt);
        var view = oauthClientPort.create(mapper.toCreateCommand(request, companyId));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toCreateResponse(view));
    }

    @GetMapping
    @Operation(summary = "Listar OAuth clients da empresa com filtros e paginação")
    @MetricsEndpoint(endpoint = "oauth_client_list", operation = "listar oauth clients")
    public ResponseEntity<PageResultView<OAuthClientResponseDTO>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) OAuthClientStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String dir) {
        oauthAdminAccess.requireAdminOrSystem(jwt);
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        var query = OAuthClientSearchQueryDTO.builder()
                .companyId(companyId)
                .clientId(clientId)
                .status(status)
                .pageable(PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(direction, sort)))
                .build();
        PageResultView<OAuthClientView> result = oauthClientPort.search(query);
        List<OAuthClientResponseDTO> content = result.getContent().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(new PageResultView<>(
                content,
                result.getPageNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                result.hasNext(),
                result.hasPrevious()
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter OAuth client por id")
    @MetricsEndpoint(endpoint = "oauth_client_get", operation = "obter oauth client")
    public ResponseEntity<OAuthClientResponseDTO> getById(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @Parameter(required = true) @PathVariable UUID id) {
        oauthAdminAccess.requireAdminOrSystem(jwt);
        return ResponseEntity.ok(mapper.toResponse(oauthClientPort.findById(companyId, id)));
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Bloquear OAuth client")
    @MetricsEndpoint(endpoint = "oauth_client_block", operation = "bloquear oauth client")
    public ResponseEntity<OAuthClientResponseDTO> block(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID id) {
        oauthAdminAccess.requireAdminOrSystem(jwt);
        return ResponseEntity.ok(mapper.toResponse(oauthClientPort.block(mapper.toIdCommand(companyId, id))));
    }

    @PostMapping("/{id}/unblock")
    @Operation(summary = "Desbloquear OAuth client")
    @MetricsEndpoint(endpoint = "oauth_client_unblock", operation = "desbloquear oauth client")
    public ResponseEntity<OAuthClientResponseDTO> unblock(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID id) {
        oauthAdminAccess.requireAdminOrSystem(jwt);
        return ResponseEntity.ok(mapper.toResponse(oauthClientPort.unblock(mapper.toIdCommand(companyId, id))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir OAuth client")
    @MetricsEndpoint(endpoint = "oauth_client_delete", operation = "excluir oauth client")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID id) {
        oauthAdminAccess.requireAdminOrSystem(jwt);
        oauthClientPort.delete(mapper.toIdCommand(companyId, id));
        return ResponseEntity.noContent().build();
    }
}
