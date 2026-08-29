package com.keepguard.ms_auth.infrastructure.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuditMdcFilterTest {

    private final JwtAuditMdcFilter filter = new JwtAuditMdcFilter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void copiesJwtClaimsToMdcDuringRequest() throws Exception {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(60),
                Map.of("alg", "none"),
                Map.of("sub", "aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa",
                        "tenant_id", "bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), (req, res) -> {
            assertEquals("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa", MDC.get("codeUser"));
            assertEquals("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", MDC.get("tenantId"));
            assertEquals("bbbbbbbb-bbbb-4bbb-8bbb-bbbbbbbbbbbb", MDC.get("companyId"));
        });

        assertNull(MDC.get("codeUser"));
    }
}
