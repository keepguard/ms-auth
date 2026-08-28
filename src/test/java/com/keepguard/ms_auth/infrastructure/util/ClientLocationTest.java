package com.keepguard.ms_auth.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientLocationTest {

    @Test
    void decodesAccentedCityFromHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Public-Location"))
                .thenReturn("Osasco%2C%20S%C3%A3o%20Paulo%2C%20Brasil");

        assertEquals("Osasco, São Paulo, Brasil", ClientLocation.from(request));
    }

    @Test
    void rejectsUnknownAndInternalLabels() {
        assertNull(ClientLocation.sanitize("Localização Desconhecida"));
        assertNull(ClientLocation.sanitize("Rede interna"));
    }
}
