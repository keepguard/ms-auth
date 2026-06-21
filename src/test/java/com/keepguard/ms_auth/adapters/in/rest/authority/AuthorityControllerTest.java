package com.keepguard.ms_auth.adapters.in.rest.authority;

import com.keepguard.ms_auth.adapters.in.rest.authority.dto.*;
import com.keepguard.ms_auth.adapters.in.rest.authority.mapper.AuthorityAdapterMapper;
import com.keepguard.ms_auth.application.dto.authority.*;
import com.keepguard.ms_auth.application.dto.common.PageResultView;
import com.keepguard.ms_auth.application.port.in.AuthorityPort;
import com.keepguard.ms_auth.domain.dto.authority.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthorityController Web Tests")
class AuthorityControllerTest {

    private MockMvc mockMvc;
    private AuthorityPort authorityPort;
    private AuthorityAdapterMapper mapper;

    @BeforeEach
    void setUp() {
        authorityPort = mock(AuthorityPort.class);
        mapper = mock(AuthorityAdapterMapper.class);
        AuthorityController controller = new AuthorityController(authorityPort, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /authorities cria com sucesso")
    void create_shouldReturnOk() throws Exception {
        var req = new AuthorityCreateRequestDTO();
        req.setName("READ_USERS");

        var createCmd = mock(AuthorityCreateCommandDTO.class);
        var view = mock(AuthorityCreateView.class);
        when(mapper.toCreateCommand(any(), any())).thenReturn(createCmd);
        when(authorityPort.create(createCmd)).thenReturn(view);
        when(mapper.toCreateResponseDTO(view)).thenReturn(
            AuthorityCreateResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("READ_USERS")
                .description("desc")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build()
        );

        mockMvc.perform(post("/api/v1/authorities")
                .header("X-Application", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"READ_USERS\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("READ_USERS"));

        verify(mapper).toCreateCommand(any(), any());
        verify(authorityPort).create(createCmd);
    }

    @Test
    @DisplayName("PUT /authorities/{id} atualiza com sucesso")
    void update_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        var updateCmd = mock(AuthorityUpdateCommandDTO.class);
        var view = mock(AuthorityUpdateView.class);
        when(mapper.toUpdateCommand(eq(id), any(), any())).thenReturn(updateCmd);
        when(authorityPort.update(updateCmd)).thenReturn(view);
        when(mapper.toUpdateResponseDTO(view)).thenReturn(
            AuthorityUpdateResponseDTO.builder()
                .id(id)
                .name("WRITE_USERS")
                .description("desc")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build()
        );

        mockMvc.perform(put("/api/v1/authorities/" + id)
                .header("X-Application", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"WRITE_USERS\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("WRITE_USERS"));
    }

    @Test
    @DisplayName("DELETE /authorities/{id} remove com sucesso")
    void delete_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        var cmd = mock(AuthorityDeleteCommandDTO.class);
        when(mapper.toDeleteCommand(eq(id), any())).thenReturn(cmd);

        mockMvc.perform(delete("/api/v1/authorities/" + id)
                .header("X-Application", UUID.randomUUID().toString()))
            .andExpect(status().isNoContent());

        verify(authorityPort).delete(cmd);
    }

    @Test
    @DisplayName("GET /authorities/{id} retorna 200 quando existe")
    void getById_shouldReturnOkWhenExists() throws Exception {
        UUID id = UUID.randomUUID();
        var query = mock(AuthorityGetByIdQueryDTO.class);
        var view = mock(AuthorityGetByIdView.class);
        when(mapper.toGetByIdQuery(eq(id), any())).thenReturn(query);
        when(authorityPort.findById(query)).thenReturn(Optional.of(view));
        when(mapper.toGetByIdResponseDTO(view)).thenReturn(
            AuthorityGetByIdResponseDTO.builder()
                .id(id)
                .name("READ_USERS")
                .description("desc")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build()
        );

        mockMvc.perform(get("/api/v1/authorities/" + id)
                .header("X-Application", UUID.randomUUID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /authorities/{id} retorna 404 quando não existe")
    void getById_shouldReturnNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        var query = mock(AuthorityGetByIdQueryDTO.class);
        when(mapper.toGetByIdQuery(eq(id), any())).thenReturn(query);
        when(authorityPort.findById(query)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/authorities/" + id)
                .header("X-Application", UUID.randomUUID().toString()))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /authorities/name/{name} retorna 200 quando existe")
    void getByName_shouldReturnOkWhenExists() throws Exception {
        String name = "READ_USERS";
        var query = mock(AuthorityGetByNameQueryDTO.class);
        var view = mock(AuthorityGetByNameView.class);
        when(mapper.toGetByNameQuery(eq(name), any())).thenReturn(query);
        when(authorityPort.findByName(query)).thenReturn(Optional.of(view));
        when(mapper.toGetByNameResponseDTO(view)).thenReturn(
            AuthorityGetByNameResponseDTO.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("desc")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build()
        );

        mockMvc.perform(get("/api/v1/authorities/name/" + name)
                .header("X-Application", UUID.randomUUID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    @DisplayName("GET /authorities retorna lista")
    void listAll_shouldReturnList() throws Exception {
        var query = mock(AuthorityGetAllQueryDTO.class);
        when(mapper.toGetAllQuery(any())).thenReturn(query);
        when(authorityPort.findAll(query)).thenReturn(List.of(mock(AuthorityListView.class)));
        when(mapper.toListResponseDTO(any())).thenReturn(
            AuthorityListResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("READ_USERS")
                .description("desc")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build()
        );

        mockMvc.perform(get("/api/v1/authorities")
                .header("X-Application", UUID.randomUUID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("READ_USERS"));
    }

    @Test
    @DisplayName("GET /authorities/search retorna página")
    void search_shouldReturnPage() throws Exception {
        var query = mock(AuthoritySearchQueryDTO.class);
        when(mapper.toSearchQuery(any(), any())).thenReturn(query);
        var view = new PageResultView<AuthoritySearchView>(List.of(mock(AuthoritySearchView.class)), 0, 10, 1, 1, true, true, false, false);
        when(authorityPort.findAll(query)).thenReturn(view);
        when(mapper.toSearchResponseDTO(any())).thenReturn(
            AuthoritySearchResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("READ_USERS")
                .description("desc")
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .build()
        );

        mockMvc.perform(get("/api/v1/authorities/search")
                .header("X-Application", UUID.randomUUID().toString())
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("READ_USERS"));
    }
}


