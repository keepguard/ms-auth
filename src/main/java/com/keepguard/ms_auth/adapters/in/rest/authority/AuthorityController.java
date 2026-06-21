package com.keepguard.ms_auth.adapters.in.rest.authority;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.adapters.in.rest.authority.dto.*;
import com.keepguard.ms_auth.adapters.in.rest.authority.mapper.AuthorityAdapterMapper;
import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.port.in.AuthorityPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/authorities")
@RequiredArgsConstructor
@Tag(name = "Authorities", description = "APIs para gerenciamento de authorities/permissões no sistema")
public class AuthorityController {

    private final AuthorityPort authorityService;
    private final AuthorityAdapterMapper mapper;

    @PostMapping
    @Operation(
        summary = "Criar authority",
        description = "Cria uma nova authority/permissão no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Authority criada com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthorityCreateResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou authority já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_create",
        operation = "criar authority"
    )
    public ResponseEntity<AuthorityCreateResponseDTO> create(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "Dados da authority a ser criada", required = true)
            @RequestBody @Valid AuthorityCreateRequestDTO dto) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Criando authority: {}, application={}", dto.getName(), xApplicationUuid);
        var command = mapper.toCreateCommand(dto, xApplicationUuid);
        var authorityView = authorityService.create(command);
        var response = mapper.toCreateResponseDTO(authorityView);
        log.info("Authority created: {} with application: {}", response.getId(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar authority",
        description = "Atualiza os dados de uma authority existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authority atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthorityUpdateResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Authority não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_update",
        operation = "atualizar authority"
    )
    public ResponseEntity<AuthorityUpdateResponseDTO> update(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "ID da authority", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Dados da authority a ser atualizada", required = true)
            @RequestBody @Valid AuthorityUpdateRequestDTO dto) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Atualizando authority: {} com nome: {}, application={}", id, dto.getName(), xApplicationUuid);
        var command = mapper.toUpdateCommand(id, dto, xApplicationUuid);
        var authorityView = authorityService.update(command);
        var response = mapper.toUpdateResponseDTO(authorityView);
        log.info("Authority updated: {} with application: {}", response.getId(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remover authority",
        description = "Remove uma authority do sistema permanentemente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Authority removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Authority não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_delete",
        operation = "remover authority"
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "ID da authority", required = true)
            @PathVariable UUID id) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Removendo authority: {}, application={}", id, xApplicationUuid);
        var command = mapper.toDeleteCommand(id, xApplicationUuid);
        authorityService.delete(command);
        log.info("Authority deleted: {} with application: {}", id, xApplicationUuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar authority por ID",
        description = "Retorna os detalhes completos de uma authority usando seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authority encontrada",
                    content = @Content(schema = @Schema(implementation = AuthorityGetByIdResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Authority não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_get_by_id",
        operation = "buscar authority por ID"
    )
    public ResponseEntity<AuthorityGetByIdResponseDTO> getById(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "ID da authority", required = true)
            @PathVariable UUID id) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Buscando authority por ID: {}, application={}", id, xApplicationUuid);
        var query = mapper.toGetByIdQuery(id, xApplicationUuid);
        Optional<AuthorityGetByIdView> authorityView = authorityService.findById(query);
        if (authorityView.isPresent()) {
            var response = mapper.toGetByIdResponseDTO(authorityView.get());
            log.info("Authority found: {} with application: {}", id, xApplicationUuid);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Authority not found: {} with application: {}", id, xApplicationUuid);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    @Operation(
        summary = "Buscar authority por nome",
        description = "Retorna os detalhes completos de uma authority usando seu nome."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authority encontrada",
                    content = @Content(schema = @Schema(implementation = AuthorityGetByNameResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Authority não encontrada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_get_by_name",
        operation = "buscar authority por nome"
    )
    public ResponseEntity<AuthorityGetByNameResponseDTO> getByName(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "Nome da authority", required = true)
            @PathVariable String name) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Buscando authority por nome: {}, application={}", name, xApplicationUuid);
        var query = mapper.toGetByNameQuery(name, xApplicationUuid);
        Optional<AuthorityGetByNameView> authorityView = authorityService.findByName(query);
        if (authorityView.isPresent()) {
            var response = mapper.toGetByNameResponseDTO(authorityView.get());
            log.info("Authority found by name: {} with application: {}", name, xApplicationUuid);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Authority not found by name: {} with application: {}", name, xApplicationUuid);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Listar authorities",
        description = "Retorna uma lista de todas as authorities do sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de authorities retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_list",
        operation = "listar authorities"
    )
    public ResponseEntity<List<AuthorityListResponseDTO>> listAll(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Listando todas as authorities, application={}", xApplicationUuid);
        var query = mapper.toGetAllQuery(xApplicationUuid);
        List<AuthorityListView> authorityViews = authorityService.findAll(query);
        List<AuthorityListResponseDTO> response = authorityViews.stream()
                .map(mapper::toListResponseDTO)
                .toList();
        log.info("Authorities listed: {} total with application: {}", response.size(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Buscar authorities com paginação",
        description = "Retorna uma página de authorities com filtros e paginação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de authorities retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_search",
        operation = "buscar authorities com paginação"
    )
    public ResponseEntity<PageResultView<AuthoritySearchResponseDTO>> search(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Valid @ModelAttribute AuthoritySearchRequestDTO searchRequest) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Buscando authorities com paginação: página {}, tamanho {}, application={}", 
                searchRequest.getPage(), searchRequest.getSize(), xApplicationUuid);
        
        var query = mapper.toSearchQuery(searchRequest, xApplicationUuid);
        PageResultView<AuthoritySearchView> pageResultView = authorityService.findAll(query);
        
        List<AuthoritySearchResponseDTO> content = pageResultView.getContent().stream()
                .map(mapper::toSearchResponseDTO)
                .toList();
        
        PageResultView<AuthoritySearchResponseDTO> response = new PageResultView<>(
                content, 
                pageResultView.getPageNumber(),
                pageResultView.getSize(),
                pageResultView.getTotalElements(),
                pageResultView.getTotalPages(),
                pageResultView.isFirst(),
                pageResultView.isLast(),
                pageResultView.hasNext(),
                pageResultView.hasPrevious()
        );
        log.info("Authorities searched: {} total with application: {}", response.getTotalElements(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }
}

