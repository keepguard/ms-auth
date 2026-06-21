package com.keepguard.ms_auth.infrastructure.filter;

import com.keepguard.ms_auth.infrastructure.context.CorrelationContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private CorrelationIdFilter correlationIdFilter;

    @Mock
    private CorrelationContext correlationContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        correlationIdFilter = new CorrelationIdFilter(correlationContext);
    }

    @Test
    @DisplayName("Deve processar requisição normal com Correlation ID do header")
    void shouldProcessNormalRequestWithCorrelationIdFromHeader() throws ServletException, IOException {
        // Given
        String correlationId = "test-correlation-id";
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn(correlationId);

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(correlationContext).setCorrelationId(correlationId);
        verify(response).setHeader(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
        verify(filterChain).doFilter(request, response);
        verify(correlationContext).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve gerar novo Correlation ID quando não existe no header")
    void shouldGenerateNewCorrelationIdWhenNotInHeader() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn(null);

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(correlationContext).setCorrelationId(anyString());
        verify(response).setHeader(eq(CorrelationContext.CORRELATION_ID_HEADER), anyString());
        verify(filterChain).doFilter(request, response);
        verify(correlationContext).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve gerar novo Correlation ID quando header está vazio")
    void shouldGenerateNewCorrelationIdWhenHeaderIsEmpty() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn("");

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(correlationContext).setCorrelationId(anyString());
        verify(response).setHeader(eq(CorrelationContext.CORRELATION_ID_HEADER), anyString());
        verify(filterChain).doFilter(request, response);
        verify(correlationContext).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve ignorar paths do actuator")
    void shouldIgnoreActuatorPaths() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(correlationContext, never()).setCorrelationId(anyString());
        verify(response, never()).setHeader(anyString(), anyString());
        verify(correlationContext, never()).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve ignorar paths do actuator com wildcard")
    void shouldIgnoreActuatorPathsWithWildcard() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(correlationContext, never()).setCorrelationId(anyString());
        verify(response, never()).setHeader(anyString(), anyString());
        verify(correlationContext, never()).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve ignorar paths do prometheus")
    void shouldIgnorePrometheusPaths() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/prometheus");

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(correlationContext, never()).setCorrelationId(anyString());
        verify(response, never()).setHeader(anyString(), anyString());
        verify(correlationContext, never()).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve ignorar paths do metrics")
    void shouldIgnoreMetricsPaths() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/actuator/metrics");

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(correlationContext, never()).setCorrelationId(anyString());
        verify(response, never()).setHeader(anyString(), anyString());
        verify(correlationContext, never()).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve limpar Correlation ID mesmo quando ocorre exceção")
    void shouldClearCorrelationIdEvenWhenExceptionOccurs() throws ServletException, IOException {
        // Given
        String correlationId = "test-correlation-id";
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn(correlationId);
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        // When & Then
        assertThrows(ServletException.class, () -> 
            correlationIdFilter.doFilterInternal(request, response, filterChain));

        // Then
        verify(correlationContext).setCorrelationId(correlationId);
        verify(response).setHeader(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
        verify(correlationContext).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve registrar logs de início e fim da requisição")
    void shouldLogRequestStartAndEnd() throws ServletException, IOException {
        // Given
        String correlationId = "test-correlation-id";
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn(correlationId);

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(correlationContext).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve processar requisição POST com Correlation ID")
    void shouldProcessPostRequestWithCorrelationId() throws ServletException, IOException {
        // Given
        String correlationId = "test-correlation-id";
        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn(correlationId);

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(correlationContext).setCorrelationId(correlationId);
        verify(response).setHeader(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
        verify(filterChain).doFilter(request, response);
        verify(correlationContext).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve processar requisição PUT com Correlation ID")
    void shouldProcessPutRequestWithCorrelationId() throws ServletException, IOException {
        // Given
        String correlationId = "test-correlation-id";
        when(request.getRequestURI()).thenReturn("/api/v1/users/123");
        when(request.getMethod()).thenReturn("PUT");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn(correlationId);

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(correlationContext).setCorrelationId(correlationId);
        verify(response).setHeader(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
        verify(filterChain).doFilter(request, response);
        verify(correlationContext).clearCorrelationId();
    }

    @Test
    @DisplayName("Deve processar requisição DELETE com Correlation ID")
    void shouldProcessDeleteRequestWithCorrelationId() throws ServletException, IOException {
        // Given
        String correlationId = "test-correlation-id";
        when(request.getRequestURI()).thenReturn("/api/v1/users/123");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader(CorrelationContext.CORRELATION_ID_HEADER)).thenReturn(correlationId);

        // When
        correlationIdFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(correlationContext).setCorrelationId(correlationId);
        verify(response).setHeader(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
        verify(filterChain).doFilter(request, response);
        verify(correlationContext).clearCorrelationId();
    }
}
