package com.keepguard.ms_auth.infrastructure.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IpAddressUtilsTest {

    @Test
    void firstIpPicksPublicAddressFromForwardedChain() {
        assertEquals("189.45.12.8", IpAddressUtils.firstIp("189.45.12.8, 10.42.0.15, 10.43.0.1"));
    }

    @Test
    void firstIpFallsBackToPrivateWhenOnlyPrivateExists() {
        assertEquals("10.42.0.15", IpAddressUtils.firstIp("10.42.0.15, 192.168.0.8"));
    }

    @Test
    void isPrivateDetectsLoopbackAndRfc1918() {
        assertTrue(IpAddressUtils.isPrivate("127.0.0.1"));
        assertTrue(IpAddressUtils.isPrivate("10.42.0.1"));
        assertTrue(IpAddressUtils.isPrivate("192.168.1.10"));
        assertFalse(IpAddressUtils.isPrivate("8.8.8.8"));
    }

    @Test
    void firstPublicIgnoresPrivateAddresses() {
        assertEquals("189.45.12.8", IpAddressUtils.firstPublic("10.42.0.1, 189.45.12.8"));
        assertNull(IpAddressUtils.firstPublic("10.42.0.1, 192.168.0.8"));
    }
}
