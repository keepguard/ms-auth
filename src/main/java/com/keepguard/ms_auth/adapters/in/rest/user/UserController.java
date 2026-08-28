package com.keepguard.ms_auth.adapters.in.rest.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.keepguard.lib_common.metrics.annotation.MetricsEndpoint;
import com.keepguard.lib_common.utils.ValidationUtils;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.request.*;
import com.keepguard.ms_auth.adapters.in.rest.user.dto.response.*;
import com.keepguard.ms_auth.adapters.in.rest.user.mapper.UserAdapterMapper;
import com.keepguard.ms_auth.application.port.in.UserPort;
import com.keepguard.ms_auth.application.service.exception.ForbiddenException;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.domain.entity.user.UserStatusHistory;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "APIs para gerenciamento de usuários, status e consultas")
public class UserController {

    private final UserPort userService;
    private final UserAdapterMapper mapper;

    @PostMapping("/create")
    @Operation(
        summary = "Criar usuário",
        description = "Cria um novo usuário no sistema com as informações fornecidas. " +
                    "O usuário será criado com status ativo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_create",
        operation = "criar usuário"
    )
    public ResponseEntity<UserResponseDTO> create(
            @Parameter(description = "Dados do usuário a ser criado", required = true)
            @Valid @RequestBody UserCreateRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Criando usuário: {},     tenantIdHeader={}", dto.getUsername(), tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var command = mapper.toCreateCommand(dto, tenantId);
        var view = userService.create(command);
        var response = mapper.toResponseDTO(view);
        
        log.info("User created: {} with application: {}", response.getId(), tenantId);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/create-admin")
    @Operation(
        summary = "Criar administrador",
        description = "Cria um usuário com apenas a ROLE_ADMIN habilitada da company."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Administrador criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe"),
        @ApiResponse(responseCode = "404", description = "ROLE_ADMIN não encontrada ou desabilitada para a company"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_create_admin",
        operation = "criar administrador"
    )
    public ResponseEntity<UserResponseDTO> createAdmin(
            @Parameter(description = "Dados do administrador a ser criado", required = true)
            @Valid @RequestBody UserCreateRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Criando admin: {}, tenantIdHeader={}", dto.getUsername(), tenantIdHeader);

        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        var command = mapper.toCreateCommand(dto, tenantId);
        var view = userService.createAdmin(command);
        var response = mapper.toResponseDTO(view);

        log.info("Admin created: {} with application: {}", response.getId(), tenantId);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/create-manager")
    @Operation(
        summary = "Criar manager",
        description = "Cria um usuário com apenas a ROLE_MANAGER habilitada da company."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Manager criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existe"),
        @ApiResponse(responseCode = "404", description = "ROLE_MANAGER não encontrada ou desabilitada para a company"),
        @ApiResponse(responseCode = "429", description = "Rate limit excedido"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_create_manager",
        operation = "criar manager"
    )
    public ResponseEntity<UserResponseDTO> createManager(
            @Parameter(description = "Dados do manager a ser criado", required = true)
            @Valid @RequestBody UserCreateRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Criando manager: {}, tenantIdHeader={}", dto.getUsername(), tenantIdHeader);

        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        var command = mapper.toCreateCommand(dto, tenantId);
        var view = userService.createManager(command);
        var response = mapper.toResponseDTO(view);

        log.info("Manager created: {} with application: {}", response.getId(), tenantId);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/delete/{idUserExternal}")
    @Operation(
        summary = "Deletar usuário",
        description = "Remove um usuário do sistema. Esta operação é irreversível."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_delete",
        operation = "deletar usuário"
    )
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID externo do usuário a ser deletado", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "Motivo da exclusão", required = true)
            @RequestBody UserStatusReasonRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Deletando usuário: {} - Motivo: {}, tenantIdHeader={}", idUserExternal, dto.getReason(), tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        var actorCodeUser = requireActorCodeUser(jwt, tenantId);
        
        var command = mapper.toDeleteCommand(idUserExternal, dto.getReason(), tenantId, actorCodeUser);
        userService.delete(command);
        
        log.info("User deleted: {} with application: {}", idUserExternal, tenantId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/hard-delete/{idUserExternal}")
    @Operation(
        summary = "Hard delete usuário",
        description = "Remove permanentemente um usuário do sistema (hard delete). " +
                    "Esta operação é irreversível e deve ser usada apenas para compensação de transações. " +
                    "Remove fisicamente o usuário, suas roles e histórico de status."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuário hard deleted com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_hard_delete",
        operation = "hard delete usuário"
    )
    public ResponseEntity<Void> hardDelete(
            @Parameter(description = "ID externo do usuário a ser hard deleted", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Hard deleting user: {}, tenantIdHeader={}", idUserExternal, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var command = mapper.toHardDeleteCommand(idUserExternal, tenantId);
        userService.hardDelete(command);
        
        log.info("User hard deleted: {} with application: {}", idUserExternal, tenantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/block/{idUserExternal}")
    @Operation(
        summary = "Bloquear usuário",
        description = "Bloqueia um usuário, impedindo que ele faça login no sistema. " +
                    "O usuário permanece no sistema mas não pode acessar."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário bloqueado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_block",
        operation = "bloquear usuário"
    )
    public ResponseEntity<Void> block(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID externo do usuário a ser bloqueado", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "Motivo do bloqueio", required = true)
            @RequestBody UserStatusReasonRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Bloqueando usuário: {} - Motivo: {}, tenantIdHeader={}", idUserExternal, dto.getReason(), tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        var actorCodeUser = requireActorCodeUser(jwt, tenantId);
        
        var command = mapper.toBlockCommand(idUserExternal, dto.getReason(), tenantId, actorCodeUser);
        userService.block(command);
        
        log.info("User blocked: {} with application: {}", idUserExternal, tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unlock/{idUserExternal}")
    @Operation(
        summary = "Desbloquear usuário",
        description = "Desbloqueia um usuário previamente bloqueado, permitindo que ele " +
                    "faça login novamente no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário desbloqueado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_unlock",
        operation = "desbloquear usuário"
    )
    public ResponseEntity<Void> unlock(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "ID externo do usuário a ser desbloqueado", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "Motivo do desbloqueio", required = true)
            @RequestBody UserStatusReasonRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Desbloqueando usuário: {} - Motivo: {}, tenantIdHeader={}", idUserExternal, dto.getReason(), tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        var actorCodeUser = requireActorCodeUser(jwt, tenantId);
        
        var command = mapper.toUnlockCommand(idUserExternal, dto.getReason(), tenantId, actorCodeUser);
        userService.unlock(command);
        
        log.info("User unlocked: {} with application: {}", idUserExternal, tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-email")
    @Operation(
        summary = "Validar email do usuário",
        description = "Marca o email de um usuário como validado. " +
                    "Esta operação é necessária para que o usuário possa acessar certas funcionalidades."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email validado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_validate_email",
        operation = "validar email do usuário"
    )
    public ResponseEntity<Void> validateEmail(
            @Parameter(description = "Dados para validação de email", required = true)
            @Valid @RequestBody UserValidateEmailRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Validando email para usuário: {}, tenantIdHeader={}", dto.getIdUserExternal(), tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var command = mapper.toValidateEmailCommand(dto, tenantId);
        userService.validateEmailUser(command);
        
        log.info("Email validated for user: {} with application: {}", dto.getIdUserExternal(), tenantId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{idUserExternal}/status-history")
    @Operation(
        summary = "Obter histórico de status do usuário",
        description = "Retorna o histórico completo de mudanças de status de um usuário, " +
                    "incluindo bloqueios, desbloqueios e outras alterações."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserStatusHistoryResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_status_history",
        operation = "obter histórico de status do usuário"
    )
    public ResponseEntity<List<UserStatusHistoryResponseDTO>> getUserStatusHistory(
            @Parameter(description = "ID externo do usuário", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "Número da página (opcional, padrão: 0)")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamanho da página (opcional, padrão: 10)")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Buscando histórico de status para usuário: {} - Página: {}, Tamanho: {}, tenantIdHeader={}", 
            idUserExternal, page, size, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var query = mapper.toGetStatusHistoryQuery(idUserExternal, page, size, tenantId);
        PageResultView<UserStatusHistory> pageResultView = userService.getUserStatusHistory(query);
        List<UserStatusHistoryResponseDTO> response = pageResultView.getContent().stream()
                .map(mapper::toStatusHistoryResponseDTO)
                .toList();
        
        log.info("User status history retrieved: {} with application: {}", idUserExternal, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code-user/{codeUser}")
    @Operation(
        summary = "Buscar usuário por código",
        description = "Retorna os detalhes completos de um usuário usando seu código único."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserByCodeResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_get_by_code",
        operation = "buscar usuário por código"
    )
    public ResponseEntity<UserByCodeResponseDTO> getByCodeUser(
            @Parameter(description = "Código único do usuário", required = true)
            @PathVariable String codeUser,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Buscando usuário por código: {}, tenantIdHeader={}", codeUser, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var query = mapper.toGetByCodeQuery(codeUser, tenantId);
        var view = userService.findByCodeUser(query);
        var response = mapper.toUserByCodeResponseDTO(view);
        
        log.info("User retrieved by code: {} with application: {}", codeUser, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/id-user-external/{idUserExternal}")
    @Operation(
        summary = "Buscar usuário por ID externo",
        description = "Retorna os detalhes completos de um usuário usando seu ID externo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserByIdExternalResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_get_by_id",
        operation = "buscar usuário por ID externo"
    )
    public ResponseEntity<UserByIdExternalResponseDTO> getByIdUserExternal(
            @Parameter(description = "ID externo do usuário", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Buscando usuário por ID externo: {}, tenantIdHeader={}", idUserExternal, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var query = mapper.toGetByIdExternalQuery(idUserExternal, tenantId);
        var view = userService.findByIdUserExternal(query);
        var response = mapper.toUserByIdExternalResponseDTO(view);
        
        log.info("User retrieved by external ID: {} with application: {}", idUserExternal, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email/{email}")
    @Operation(
        summary = "Buscar usuário por email",
        description = "Retorna os detalhes completos de um usuário usando seu endereço de email."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserByEmailResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_get_by_email",
        operation = "buscar usuário por email"
    )
    public ResponseEntity<UserByEmailResponseDTO> getByEmail(
            @Parameter(description = "Email do usuário", required = true)
            @PathVariable String email,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Buscando usuário por email: {}, tenantIdHeader={}", email, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var query = mapper.toGetByEmailQuery(email, tenantId);
        var view = userService.findByEmail(query);
        var response = mapper.toUserByEmailResponseDTO(view);
        
        log.info("User retrieved by email: {} with application: {}", email, tenantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    @Operation(
        summary = "Buscar usuário por username",
        description = "Retorna os detalhes completos de um usuário usando seu nome de usuário."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserByUsernameResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_get_by_username",
        operation = "buscar usuário por username"
    )
    public ResponseEntity<UserByUsernameResponseDTO> getByUsername(
            @Parameter(description = "Nome de usuário", required = true)
            @PathVariable String username,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Buscando usuário por username: {}, tenantIdHeader={}", username, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var query = mapper.toGetByUsernameQuery(username, tenantId);
        var view = userService.findByUsername(query);
        var response = mapper.toUserByUsernameResponseDTO(view);
        
        log.info("User retrieved by username: {} with application: {}", username, tenantId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{idUserExternal}/roles")
    @Operation(
        summary = "Adicionar role ao usuário",
        description = "Associa uma role específica a um usuário pelo nome da role. " +
                    "O usuário ganha as permissões associadas à role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Role adicionada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário ou role não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou role já associada"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_add_role",
        operation = "adicionar role ao usuário"
    )
    public ResponseEntity<Void> addRoleToUser(
            @Parameter(description = "ID externo do usuário", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "Dados da role a ser adicionada (nome)", required = true)
            @Valid @RequestBody UserAddRoleToUserRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Adicionando role {} ao usuário: {}, tenantIdHeader={}", dto.getRole(), idUserExternal, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var command = mapper.toAddRoleCommand(idUserExternal, dto.getRole(), tenantId);
        userService.addRoleToUser(command);
        
        log.info("Role added to user: {} - {} with application: {}", idUserExternal, dto.getRole(), tenantId);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{idUserExternal}/roles/{role}")
    @Operation(
        summary = "Remover role do usuário",
        description = "Remove uma role específica de um usuário pelo nome da role. " +
                    "O usuário perde as permissões associadas à role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role removida com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário ou role não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_remove_role",
        operation = "remover role do usuário"
    )
    public ResponseEntity<Void> removeRoleFromUser(
            @Parameter(description = "ID externo do usuário", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "Nome da role a ser removida", required = true)
            @PathVariable String role,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Removendo role {} do usuário: {}, tenantIdHeader={}", role, idUserExternal, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var command = mapper.toRemoveRoleCommand(idUserExternal, role, tenantId);
        userService.removeRoleFromUser(command);
        
        log.info("Role removed from user: {} - {} with application: {}", idUserExternal, role, tenantId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{idUserExternal}/email")
    @Operation(
        summary = "Atualizar email do usuário",
        description = "Atualiza o endereço de email de um usuário ativo. " +
                    "O email será marcado como não verificado após a alteração. " +
                    "Não é possível alterar para um email já existente no sistema."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos, usuário não ativo ou email já existe"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_update_email",
        operation = "atualizar email do usuário"
    )
    public ResponseEntity<Void> updateUserEmail(
            @Parameter(description = "ID externo do usuário", required = true)
            @PathVariable String idUserExternal,
            @Parameter(description = "Dados para atualização de email", required = true)
            @Valid @RequestBody UserUpdateEmailRequestDTO dto,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Atualizando email do usuário: {} - Novo email: {}, tenantIdHeader={}", idUserExternal, dto.getNewEmail(), tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var command = mapper.toUpdateEmailCommand(idUserExternal, dto.getNewEmail(), tenantId);
        userService.updateUserEmail(command);
        
        log.info("Email updated for user: {} - New email: {} with application: {}", idUserExternal, dto.getNewEmail(), tenantId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/search")
    @Operation(
        summary = "Buscar usuários",
        description = "Busca usuários com filtros dinâmicos e paginação"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros de busca inválidos"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @MetricsEndpoint(
        endpoint = "user_search",
        operation = "buscar usuários"
    )
    public ResponseEntity<PageResultView<UserResponseDTO>> search(
            @Parameter(description = "ID do usuário") @RequestParam(required = false) String id,
            @Parameter(description = "Username (busca parcial)") @RequestParam(required = false) String username,
            @Parameter(description = "Email (busca parcial)") @RequestParam(required = false) String email,
            @Parameter(description = "ID externo do usuário") @RequestParam(required = false) String idUserExternal,
            @Parameter(description = "Código do usuário") @RequestParam(required = false) String codeUser,
            @Parameter(description = "Status do usuário") @RequestParam(required = false) String status,
            @Parameter(description = "Email verificado") @RequestParam(required = false) Boolean emailVerified,
            @Parameter(description = "ID da empresa") @RequestParam(required = false) String companyId,
            @Parameter(description = "Código da empresa") @RequestParam(required = false) String companyCode,
            @Parameter(description = "Campo de ordenação") @RequestParam(required = false) String sortBy,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "UUID da aplicação", required = true)
            @RequestHeader("X-Tenant-Id") String tenantIdHeader) {

        log.info("Buscando usuários - username: {}, email: {}, status: {}, companyCode: {}, tenantIdHeader={}", 
            username, email, status, companyCode, tenantIdHeader);
        
        var tenantId = ValidationUtils.validateTenantId(tenantIdHeader);
        
        var searchRequest = UserSearchRequestDTO.builder()
                .id(id != null ? java.util.UUID.fromString(id) : null)
                .username(username)
                .email(email)
                .idUserExternal(idUserExternal != null ? java.util.UUID.fromString(idUserExternal) : null)
                .codeUser(codeUser != null ? java.util.UUID.fromString(codeUser) : null)
                .status(status != null ? com.keepguard.ms_auth.domain.enums.UserStatus.valueOf(status.toUpperCase()) : null)
                .emailVerified(emailVerified)
                .companyId(companyId != null ? java.util.UUID.fromString(companyId) : null)
                .companyCode(companyCode != null ? java.util.UUID.fromString(companyCode) : null)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();
        
        var query = mapper.toSearchQuery(searchRequest, tenantId);
        var pageResult = userService.searchUsers(query);
        
        var response = PageResultView.<UserResponseDTO>builder()
                .content(pageResult.getContent().stream()
                        .map(mapper::toResponseDTO)
                        .toList())
                .page(pageResult.getPageNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .hasNext(pageResult.hasNext())
                .hasPrevious(pageResult.hasPrevious())
                .build();
        
        log.info("Users search completed. Found {} results with application: {}", response.getTotalElements(), tenantId);
        return ResponseEntity.ok(response);
    }

    private String requireActorCodeUser(Jwt jwt, UUID tenantId) {
        if (jwt == null) {
            throw new ForbiddenException("Token JWT não informado ou inválido.", "JWT_REQUIRED");
        }
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new ForbiddenException("Token JWT sem subject.", "JWT_INVALID");
        }
        String tokenTenant = jwt.getClaimAsString("tenant_id");
        if (tokenTenant != null && !tokenTenant.equalsIgnoreCase(tenantId.toString())) {
            throw new ForbiddenException("Tenant do token não corresponde ao header.", "TENANT_MISMATCH");
        }
        return subject;
    }
}