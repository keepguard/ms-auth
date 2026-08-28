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
}
