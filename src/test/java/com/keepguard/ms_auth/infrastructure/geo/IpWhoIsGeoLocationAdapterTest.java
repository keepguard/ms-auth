package com.keepguard.ms_auth.infrastructure.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpWhoIsGeoLocationAdapterTest {

    @Test
    void formatsCityRegionAndCountryInPortuguese() {
        assertEquals("Osasco, São Paulo, Brasil",
                IpWhoIsGeoLocationAdapter.format("Osasco", "São Paulo", "Brazil", "BR"));
    }

    @Test
    void omitsRegionWhenItRepeatsTheCity() {
        assertEquals("São Paulo, Brasil",
                IpWhoIsGeoLocationAdapter.format("São Paulo", "São Paulo", "Brazil", "BR"));
    }

    @Test
    void returnsUnknownWhenLocalEnabledIsTrueWithoutExternalCalls() {
        var adapter = new IpWhoIsGeoLocationAdapter(
                new com.fasterxml.jackson.databind.ObjectMapper(),
                null,
                "https://lookup/%s",
                "https://fallback/%s",
                true
        );
        assertEquals(IpWhoIsGeoLocationAdapter.UNKNOWN, adapter.resolve("200.100.50.25"));
    }
}
