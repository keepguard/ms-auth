package com.keepguard.ms_auth.adapters.in.rest.role;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.adapters.in.rest.role.dto.*;
import com.keepguard.ms_auth.application.dto.role.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.adapters.in.rest.role.mapper.RoleAdapterMapper;
import com.keepguard.ms_auth.application.port.in.RolePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "APIs para gerenciamento de roles no sistema")
public class RoleController {

    private final RolePort roleService;
    private final RoleAdapterMapper mapper;

    @PostMapping
    @Operation(
        summary = "Criar role",
        description = "Cria um novo role no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Role criado com sucesso",
                    content = @Content(schema = @Schema(implementation = RoleCreateResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou role já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_create",
        operation = "criar role"
    )
    public ResponseEntity<RoleCreateResponseDTO> create(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "Dados do role a ser criado", required = true)
            @RequestBody @Valid RoleCreateDTO dto) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Criando role: {}, application={}", dto.getName(), xApplicationUuid);
        var command = mapper.toCreateCommand(dto, xApplicationUuid);
        var roleView = roleService.create(command);
        var response = mapper.toCreateResponseDTO(roleView);
        log.info("Role created: {} with application: {}", response.getId(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Atualizar role",
        description = "Atualiza os dados de um role existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = RoleUpdateResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Role não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_update",
        operation = "atualizar role"
    )
    public ResponseEntity<RoleUpdateResponseDTO> update(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "ID do role", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Dados do role a ser atualizado", required = true)
            @RequestBody @Valid RoleUpdateDTO dto) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Atualizando role: {} com nome: {}, application={}", id, dto.getName(), xApplicationUuid);
        var command = mapper.toUpdateCommand(id, dto, xApplicationUuid);
        var roleView = roleService.update(command);
        var response = mapper.toUpdateResponseDTO(roleView);
        log.info("Role updated: {} with application: {}", response.getId(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Remover role",
        description = "Remove um role do sistema permanentemente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Role não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_delete",
        operation = "remover role"
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "ID do role", required = true)
            @PathVariable UUID id) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Removendo role: {}, application={}", id, xApplicationUuid);
        var command = mapper.toDeleteCommand(id, xApplicationUuid);
        roleService.delete(command);
        log.info("Role deleted: {} with application: {}", id, xApplicationUuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Buscar role por ID",
        description = "Retorna os detalhes completos de um role usando seu ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role encontrado",
                    content = @Content(schema = @Schema(implementation = RoleGetByIdResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_get_by_id",
        operation = "buscar role por ID"
    )
    public ResponseEntity<RoleGetByIdResponseDTO> getById(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "ID do role", required = true)
            @PathVariable UUID id) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Buscando role por ID: {}, application={}", id, xApplicationUuid);
        var command = mapper.toGetByIdCommand(id, xApplicationUuid);
        Optional<RoleGetByIdView> roleView = roleService.findById(command);
        if (roleView.isPresent()) {
            var response = mapper.toGetByIdResponseDTO(roleView.get());
            log.info("Role found: {} with application: {}", id, xApplicationUuid);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Role not found: {} with application: {}", id, xApplicationUuid);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name/{name}")
    @Operation(
        summary = "Buscar role por nome",
        description = "Retorna os detalhes completos de um role usando seu nome."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role encontrado",
                    content = @Content(schema = @Schema(implementation = RoleGetByNameResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Role não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_get_by_name",
        operation = "buscar role por nome"
    )
    public ResponseEntity<RoleGetByNameResponseDTO> getByName(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "Nome do role", required = true)
            @PathVariable String name) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Buscando role por nome: {}, application={}", name, xApplicationUuid);
        var command = mapper.toGetByNameCommand(name, xApplicationUuid);
        Optional<RoleGetByNameView> roleView = roleService.findByName(command);
        if (roleView.isPresent()) {
            var response = mapper.toGetByNameResponseDTO(roleView.get());
            log.info("Role found by name: {} with application: {}", name, xApplicationUuid);
            return ResponseEntity.ok(response);
        } else {
            log.warn("Role not found by name: {} with application: {}", name, xApplicationUuid);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(
        summary = "Listar roles",
        description = "Retorna uma lista de todos os roles do sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de roles retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_list",
        operation = "listar roles"
    )
    public ResponseEntity<List<RoleListResponseDTO>> listAll(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Listando todas as roles, application={}", xApplicationUuid);
        var command = mapper.toGetAllCommand(xApplicationUuid);
        List<RoleListView> roleViews = roleService.findAll(command);
        List<RoleListResponseDTO> response = roleViews.stream()
                .map(mapper::toListResponseDTO)
                .toList();
        log.info("Roles listed: {} total with application: {}", response.size(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(
        summary = "Buscar roles com paginação",
        description = "Retorna uma página de roles com paginação."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de roles retornada com sucesso"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_search",
        operation = "buscar roles com paginação"
    )
    public ResponseEntity<PageResultView<RoleSearchResponseDTO>> search(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            Pageable pageable) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Buscando roles com paginação: página {}, tamanho {}, application={}", 
                pageable.getPageNumber(), pageable.getPageSize(), xApplicationUuid);
        var command = mapper.toSearchCommand(pageable, xApplicationUuid);
        PageResultView<RoleSearchView> pageResultView = roleService.findAll(command);
        List<RoleSearchResponseDTO> content = pageResultView.getContent().stream()
                .map(mapper::toSearchResponseDTO)
                .toList();
        PageResultView<RoleSearchResponseDTO> response = new PageResultView<>(
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
        log.info("Roles searched: {} total with application: {}", response.getTotalElements(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/authorities/add")
    @Operation(
        summary = "Adicionar authority ao role",
        description = "Adiciona uma authority existente a um role específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authority adicionada com sucesso",
                    content = @Content(schema = @Schema(implementation = RoleAddAuthorityResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Role ou Authority não encontrado"),
        @ApiResponse(responseCode = "409", description = "Authority já está associada ao role"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_add_authority",
        operation = "adicionar authority ao role"
    )
    public ResponseEntity<RoleAddAuthorityResponseDTO> addAuthority(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "Dados para adicionar authority ao role", required = true)
            @RequestBody @Valid RoleAddAuthorityRequestDTO dto) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Adicionando authority {} ao role: {}, application={}", 
                dto.getAuthorityName(), dto.getRoleId(), xApplicationUuid);
        
        var command = mapper.toAddAuthorityCommand(dto, xApplicationUuid);
        var roleView = roleService.addAuthority(command);
        var response = mapper.toAddAuthorityResponseDTO(roleView);
        
        log.info("Authority {} adicionada ao role: {} with application: {}", 
                dto.getAuthorityName(), dto.getRoleId(), xApplicationUuid);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/authorities/remove")
    @Operation(
        summary = "Remover authority do role",
        description = "Remove uma authority de um role específico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authority removida com sucesso",
                    content = @Content(schema = @Schema(implementation = RoleRemoveAuthorityResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Role ou Authority não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "role_remove_authority",
        operation = "remover authority do role"
    )
    public ResponseEntity<RoleRemoveAuthorityResponseDTO> removeAuthority(
            @Parameter(description = "Identificador da aplicação", required = true)
            @RequestHeader("X-Application") String xApplication,
            @Parameter(description = "Dados para remover authority do role", required = true)
            @RequestBody @Valid RoleRemoveAuthorityRequestDTO dto) {

        var xApplicationUuid = ValidationUtils.validateXApplication(xApplication);
        log.info("Removendo authority {} do role: {}, application={}", 
                dto.getAuthorityName(), dto.getRoleId(), xApplicationUuid);
        
        var command = mapper.toRemoveAuthorityCommand(dto, xApplicationUuid);
        var roleView = roleService.removeAuthority(command);
        var response = mapper.toRemoveAuthorityResponseDTO(roleView);
        
        log.info("Authority {} removida do role: {} with application: {}", 
                dto.getAuthorityName(), dto.getRoleId(), xApplicationUuid);
        return ResponseEntity.ok(response);
    }
}