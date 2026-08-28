package com.keepguard.ms_auth.infrastructure.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class ClientLocation {

    private ClientLocation() {
    }

    public static String from(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return sanitize(request.getHeader("X-Public-Location"));
    }

    public static String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        try {
            value = URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // header already decoded or invalid; keep original
        }
        value = value.replaceAll("[\\p{Cntrl}<>]", " ").replaceAll("\\s+", " ").trim();
        if (value.length() > 120) {
            value = value.substring(0, 120).trim();
        }
        return isUsable(value) ? value : null;
    }

    public static boolean isUsable(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        return !"Localização Desconhecida".equalsIgnoreCase(location)
                && !"Rede interna".equalsIgnoreCase(location);
    }
}
