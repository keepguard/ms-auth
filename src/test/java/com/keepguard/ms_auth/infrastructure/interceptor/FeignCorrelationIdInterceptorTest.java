package com.keepguard.ms_auth.infrastructure.interceptor;

import com.keepguard.ms_auth.infrastructure.context.CorrelationContext;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeignCorrelationIdInterceptorTest {

    private FeignCorrelationIdInterceptor interceptor;

    @Mock
    private CorrelationContext correlationContext;

    @Mock
    private RequestTemplate requestTemplate;

    @BeforeEach
    void setUp() {
        interceptor = new FeignCorrelationIdInterceptor(correlationContext);
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID quando disponível")
    void shouldAddCorrelationIdWhenAvailable() {
        // Given
        String correlationId = "test-correlation-id";
        String url = "http://example.com/api/test";
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID quando não vazio")
    void shouldAddCorrelationIdWhenNotEmpty() {
        // Given
        String correlationId = "  test-correlation-id  ";
        String url = "http://example.com/api/test";
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }

    @Test
    @DisplayName("Não deve adicionar header quando Correlation ID é null")
    void shouldNotAddHeaderWhenCorrelationIdIsNull() {
        // Given
        String url = "http://example.com/api/test";
        when(correlationContext.getCorrelationId()).thenReturn(null);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate, never()).header(anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve adicionar header quando Correlation ID está vazio")
    void shouldNotAddHeaderWhenCorrelationIdIsEmpty() {
        // Given
        String url = "http://example.com/api/test";
        when(correlationContext.getCorrelationId()).thenReturn("");
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate, never()).header(anyString(), anyString());
    }

    @Test
    @DisplayName("Não deve adicionar header quando Correlation ID contém apenas espaços")
    void shouldNotAddHeaderWhenCorrelationIdIsOnlyWhitespace() {
        // Given
        String url = "http://example.com/api/test";
        when(correlationContext.getCorrelationId()).thenReturn("   ");
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate, never()).header(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID para diferentes URLs")
    void shouldAddCorrelationIdForDifferentUrls() {
        // Given
        String correlationId = "test-correlation-id";
        String url1 = "http://service1.com/api/users";
        String url2 = "http://service2.com/api/orders";
        
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);

        // When
        when(requestTemplate.url()).thenReturn(url1);
        interceptor.apply(requestTemplate);
        
        when(requestTemplate.url()).thenReturn(url2);
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate, times(2)).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID para URL com query parameters")
    void shouldAddCorrelationIdForUrlWithQueryParameters() {
        // Given
        String correlationId = "test-correlation-id";
        String url = "http://example.com/api/users?page=1&size=10";
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID para URL com path parameters")
    void shouldAddCorrelationIdForUrlWithPathParameters() {
        // Given
        String correlationId = "test-correlation-id";
        String url = "http://example.com/api/users/123/orders/456";
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID para URL HTTPS")
    void shouldAddCorrelationIdForHttpsUrl() {
        // Given
        String correlationId = "test-correlation-id";
        String url = "https://secure.example.com/api/test";
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID para URL com porta")
    void shouldAddCorrelationIdForUrlWithPort() {
        // Given
        String correlationId = "test-correlation-id";
        String url = "http://example.com:8080/api/test";
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }

    @Test
    @DisplayName("Deve adicionar Correlation ID para URL com fragment")
    void shouldAddCorrelationIdForUrlWithFragment() {
        // Given
        String correlationId = "test-correlation-id";
        String url = "http://example.com/api/test#section";
        when(correlationContext.getCorrelationId()).thenReturn(correlationId);
        when(requestTemplate.url()).thenReturn(url);

        // When
        interceptor.apply(requestTemplate);

        // Then
        verify(requestTemplate).header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
    }
}
