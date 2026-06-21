package com.keepguard.ms_auth.adapters.in.rest.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta paginada da busca de usuários")
public class UserSearchResponseDTO {

    @Schema(description = "Lista de usuários encontrados")
    private List<com.keepguard.ms_auth.adapters.in.rest.user.dto.response.UserDetailsResponseDTO> content;

    @Schema(description = "Número da página atual",
            example = "0")
    private Integer pageNumber;

    @Schema(description = "Tamanho da página",
            example = "10")
    private Integer pageSize;

    @Schema(description = "Número total de elementos",
            example = "150")
    private Long totalElements;

    @Schema(description = "Número total de páginas",
            example = "15")
    private Integer totalPages;

    @Schema(description = "Indica se é a primeira página",
            example = "true")
    private Boolean first;

    @Schema(description = "Indica se é a última página",
            example = "false")
    private Boolean last;

    @Schema(description = "Indica se há próxima página",
            example = "true")
    private Boolean hasNext;

    @Schema(description = "Indica se há página anterior",
            example = "false")
    private Boolean hasPrevious;
}