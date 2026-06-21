package com.keepguard.ms_auth.infrastructure.interceptor;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeignErrorDecoderTest {

    private FeignErrorDecoder errorDecoder;

    @Mock
    private Response response;
    
    @Mock
    private Request request;

    @BeforeEach
    void setUp() {
        errorDecoder = new FeignErrorDecoder();
        when(response.request()).thenReturn(request);
        when(request.httpMethod()).thenReturn(Request.HttpMethod.GET);
        when(request.url()).thenReturn("http://example.com/api/test");
    }

    @Test
    @DisplayName("Deve decodificar erro 400 Bad Request")
    void shouldDecodeBadRequestError() {
        // Given
        String methodKey = "UserClient#getUser(String)";
        when(response.status()).thenReturn(400);
        when(response.reason()).thenReturn("Bad Request");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(400, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro 401 Unauthorized")
    void shouldDecodeUnauthorizedError() {
        // Given
        String methodKey = "AuthClient#login(LoginRequest)";
        when(response.status()).thenReturn(401);
        when(response.reason()).thenReturn("Unauthorized");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(401, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro 403 Forbidden")
    void shouldDecodeForbiddenError() {
        // Given
        String methodKey = "UserClient#updateUser(String, UserRequest)";
        when(response.status()).thenReturn(403);
        when(response.reason()).thenReturn("Forbidden");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(403, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro 404 Not Found")
    void shouldDecodeNotFoundError() {
        // Given
        String methodKey = "UserClient#getUser(String)";
        when(response.status()).thenReturn(404);
        when(response.reason()).thenReturn("Not Found");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(404, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro 500 Internal Server Error")
    void shouldDecodeInternalServerError() {
        // Given
        String methodKey = "UserClient#createUser(UserRequest)";
        when(response.status()).thenReturn(500);
        when(response.reason()).thenReturn("Internal Server Error");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(500, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro 502 Bad Gateway")
    void shouldDecodeBadGatewayError() {
        // Given
        String methodKey = "ExternalClient#callExternalService()";
        when(response.status()).thenReturn(502);
        when(response.reason()).thenReturn("Bad Gateway");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(502, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro 503 Service Unavailable")
    void shouldDecodeServiceUnavailableError() {
        // Given
        String methodKey = "ExternalClient#callExternalService()";
        when(response.status()).thenReturn(503);
        when(response.reason()).thenReturn("Service Unavailable");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(503, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro 504 Gateway Timeout")
    void shouldDecodeGatewayTimeoutError() {
        // Given
        String methodKey = "ExternalClient#callExternalService()";
        when(response.status()).thenReturn(504);
        when(response.reason()).thenReturn("Gateway Timeout");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(504, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro com reason null")
    void shouldDecodeErrorWithNullReason() {
        // Given
        String methodKey = "UserClient#getUser(String)";
        when(response.status()).thenReturn(400);
        when(response.reason()).thenReturn(null);

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(400, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro com methodKey complexo")
    void shouldDecodeErrorWithComplexMethodKey() {
        // Given
        String methodKey = "com.keepguard.ms_auth.adapters.out.feign.UserClient#getUserById(java.util.UUID)";
        when(response.status()).thenReturn(404);
        when(response.reason()).thenReturn("Not Found");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(404, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro com status 0")
    void shouldDecodeErrorWithStatusZero() {
        // Given
        String methodKey = "UserClient#getUser(String)";
        when(response.status()).thenReturn(0);
        when(response.reason()).thenReturn("Unknown Error");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(0, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro com status negativo")
    void shouldDecodeErrorWithNegativeStatus() {
        // Given
        String methodKey = "UserClient#getUser(String)";
        when(response.status()).thenReturn(-1);
        when(response.reason()).thenReturn("Connection Error");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(-1, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro com reason vazio")
    void shouldDecodeErrorWithEmptyReason() {
        // Given
        String methodKey = "UserClient#getUser(String)";
        when(response.status()).thenReturn(500);
        when(response.reason()).thenReturn("");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(500, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro com methodKey vazio")
    void shouldDecodeErrorWithEmptyMethodKey() {
        // Given
        String methodKey = "";
        when(response.status()).thenReturn(500);
        when(response.reason()).thenReturn("Internal Server Error");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(500, ((FeignException) exception).status());
    }

    @Test
    @DisplayName("Deve decodificar erro com methodKey null")
    void shouldDecodeErrorWithNullMethodKey() {
        // Given
        String methodKey = null;
        when(response.status()).thenReturn(500);
        when(response.reason()).thenReturn("Internal Server Error");

        // When
        Exception exception = errorDecoder.decode(methodKey, response);

        // Then
        assertNotNull(exception);
        assertTrue(exception instanceof FeignException);
        assertEquals(500, ((FeignException) exception).status());
    }
}
