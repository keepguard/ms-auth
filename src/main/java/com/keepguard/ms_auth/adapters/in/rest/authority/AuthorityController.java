package com.keepguard.ms_auth.adapters.in.rest.authority;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
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
@Tag(name = "Authorities", description = "APIs para gerenciamento do catálogo global de authorities/permissões")
public class AuthorityController {

    private final AuthorityPort authorityService;
    private final AuthorityAdapterMapper mapper;

    @PostMapping
    @Operation(
        summary = "Criar authority",
        description = "Cria uma nova authority/permissão no catálogo global."
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
            @Parameter(description = "Dados da authority a ser criada", required = true)
            @RequestBody @Valid AuthorityCreateRequestDTO dto) {

        log.info("Criando authority: {}", dto.getName());
        var command = mapper.toCreateCommand(dto);
        var authorityView = authorityService.create(command);
        var response = mapper.toCreateResponseDTO(authorityView);
        log.info("Authority created: {}", response.getId());
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
            @Parameter(description = "ID da authority", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Dados da authority a ser atualizada", required = true)
            @RequestBody @Valid AuthorityUpdateRequestDTO dto) {

        log.info("Atualizando authority: {} com nome: {}", id, dto.getName());
        var command = mapper.toUpdateCommand(id, dto);
        var authorityView = authorityService.update(command);
        var response = mapper.toUpdateResponseDTO(authorityView);
        log.info("Authority updated: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remover authority",
        description = "Remove uma authority do catálogo global permanentemente."
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
            @Parameter(description = "ID da authority", required = true)
            @PathVariable UUID id) {

        log.info("Removendo authority: {}", id);
        var command = mapper.toDeleteCommand(id);
        authorityService.delete(command);
        log.info("Authority deleted: {}", id);
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
            @Parameter(description = "ID da authority", required = true)
            @PathVariable UUID id) {

        log.info("Buscando authority por ID: {}", id);
        var query = mapper.toGetByIdQuery(id);
        Optional<AuthorityGetByIdView> authorityView = authorityService.findById(query);
        if (authorityView.isPresent()) {
            var response = mapper.toGetByIdResponseDTO(authorityView.get());
            log.info("Authority found: {}", id);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Authority not found: {}", id);
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
            @Parameter(description = "Nome da authority", required = true)
            @PathVariable String name) {

        log.info("Buscando authority por nome: {}", name);
        var query = mapper.toGetByNameQuery(name);
        Optional<AuthorityGetByNameView> authorityView = authorityService.findByName(query);
        if (authorityView.isPresent()) {
            var response = mapper.toGetByNameResponseDTO(authorityView.get());
            log.info("Authority found by name: {}", name);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Authority not found by name: {}", name);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Listar authorities",
        description = "Retorna o catálogo global de authorities."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de authorities retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "authority_list",
        operation = "listar authorities"
    )
    public ResponseEntity<List<AuthorityListResponseDTO>> listAll() {

        log.info("Listando todas as authorities");
        var query = mapper.toGetAllQuery();
        List<AuthorityListView> authorityViews = authorityService.findAll(query);
        List<AuthorityListResponseDTO> response = authorityViews.stream()
                .map(mapper::toListResponseDTO)
                .toList();
        log.info("Authorities listed: {} total", response.size());
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
            @Valid @ModelAttribute AuthoritySearchRequestDTO searchRequest) {

        log.info("Buscando authorities com paginação: página {}, tamanho {}",
                searchRequest.getPage(), searchRequest.getSize());
        
        var query = mapper.toSearchQuery(searchRequest);
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
        log.info("Authorities searched: {} total", response.getTotalElements());
        return ResponseEntity.ok(response);
    }
}
