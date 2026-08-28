package com.keepguard.ms_auth.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientIpResolverTest {

    @Test
    void prefersPublicClientIpHeaderOverPrivateForwardedChain() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Client-IP")).thenReturn("189.45.12.8");
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.42.0.1");
        when(request.getRemoteAddr()).thenReturn("10.42.0.15");

        assertEquals("189.45.12.8", ClientIpResolver.from(request));
    }

    @Test
    void picksFirstPublicAddressFromForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("189.45.12.8, 10.42.0.1");
        when(request.getRemoteAddr()).thenReturn("10.42.0.15");

        assertEquals("189.45.12.8", ClientIpResolver.from(request));
    }

    @Test
    void fallsBackToPrivateWhenNoPublicIpExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.42.0.1");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        assertEquals("10.42.0.1", ClientIpResolver.from(request));
    }

    @Test
    void returnsNullWhenRequestIsNull() {
        assertNull(ClientIpResolver.from(null));
    }
}
