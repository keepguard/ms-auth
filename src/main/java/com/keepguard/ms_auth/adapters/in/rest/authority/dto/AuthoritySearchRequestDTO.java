package com.keepguard.ms_auth.adapters.in.rest.authority.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parâmetros de busca para authorities")
public class AuthoritySearchRequestDTO {

    @Schema(description = "Nome da authority (busca parcial)", example = "USER")
    private String name;

    @Schema(description = "Descrição da authority (busca parcial)", example = "permissão")
    private String description;

    @Schema(description = "Número da página (opcional, padrão: 0)", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Tamanho da página (opcional, padrão: 10)", example = "10")
    @Builder.Default
    private Integer size = 10;

    @Schema(description = "Campo para ordenação dos resultados. " +
            "Campos válidos: id, name, description, createdAt, updatedAt. " +
            "Padrão: createdAt",
            example = "createdAt",
            allowableValues = {"id", "name", "description", "createdAt", "updatedAt"})
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "Direção da ordenação. " +
            "Valores válidos: ASC (crescente), DESC (decrescente). " +
            "Padrão: DESC",
            example = "DESC",
            allowableValues = {"ASC", "DESC"})
    @Builder.Default
    private String sortDirection = "DESC";
}

