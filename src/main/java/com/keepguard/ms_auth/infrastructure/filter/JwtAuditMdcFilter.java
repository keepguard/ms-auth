package com.keepguard.ms_auth.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuditMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String subject = jwt.getSubject();
                if (subject != null && !subject.isBlank()) {
                    MDC.put("codeUser", subject);
                }
                String tenantId = firstNonBlank(
                        jwt.getClaimAsString("tenant_id"),
                        jwt.getClaimAsString("tenantId"));
                if (tenantId != null) {
                    MDC.put("tenantId", tenantId);
                    MDC.put("companyId", tenantId);
                }
            }
            String headerTenant = request.getHeader("X-Tenant-Id");
            if (isBlank(MDC.get("tenantId")) && !isBlank(headerTenant)) {
                MDC.put("tenantId", headerTenant.trim());
            }
            String headerCompany = request.getHeader("X-Company-Id");
            if (!isBlank(headerCompany)) {
                MDC.put("companyId", headerCompany.trim());
            } else if (isBlank(MDC.get("companyId")) && !isBlank(MDC.get("tenantId"))) {
                MDC.put("companyId", MDC.get("tenantId"));
            }
            if (isBlank(MDC.get("tenantId")) && !isBlank(MDC.get("companyId"))) {
                MDC.put("tenantId", MDC.get("companyId"));
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("codeUser");
            MDC.remove("tenantId");
            MDC.remove("companyId");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
