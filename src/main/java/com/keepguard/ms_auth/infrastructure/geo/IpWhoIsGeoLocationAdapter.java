package com.keepguard.ms_auth.infrastructure.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.adapters.out.feign.GeoLocationClient;
import com.keepguard.ms_auth.application.port.out.geo.GeoLocationPort;
import com.keepguard.ms_auth.infrastructure.util.IpAddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class IpWhoIsGeoLocationAdapter implements GeoLocationPort {

    static final String UNKNOWN = "Localização Desconhecida";
    static final String INTERNAL = "Rede interna";

    private final ObjectMapper objectMapper;
    private final GeoLocationClient geoLocationClient;
    private final String lookupUrlTemplate;
    private final String fallbackUrlTemplate;
    private final boolean localEnabled;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public IpWhoIsGeoLocationAdapter(
            ObjectMapper objectMapper,
            GeoLocationClient geoLocationClient,
            @Value("${app.geo.lookup-url:https://get.geojs.io/v1/ip/geo/%s.json}") String lookupUrlTemplate,
            @Value("${app.geo.fallback-url:https://ipwho.is/%s?fields=success,city,region,country,country_code}") String fallbackUrlTemplate,
            @Value("${app.geo.local-enabled:false}") boolean localEnabled
    ) {
        this.objectMapper = objectMapper;
        this.geoLocationClient = geoLocationClient;
        this.lookupUrlTemplate = lookupUrlTemplate;
        this.fallbackUrlTemplate = fallbackUrlTemplate;
        this.localEnabled = localEnabled;
    }

    @Override
    public String resolve(String ipAddress) {
        String ip = IpAddressUtils.firstIp(ipAddress);
        if (ip == null) {
            return UNKNOWN;
        }
        if (IpAddressUtils.isPrivate(ip)) {
            return INTERNAL;
        }
        return cache.computeIfAbsent(ip, this::lookup);
    }

    private String lookup(String ip) {
        if (localEnabled) {
            log.debug("GeoIP operando em modo local soberano (app.geo.local-enabled=true). Sem requisições de egress externo para IP.");
            return UNKNOWN;
        }
        log.debug("Egress GeoIP externo acionado para resolução de IP (LGPD Art. 33)");
        String primary = lookupUrl(lookupUrlTemplate, ip);
        if (isResolved(primary)) {
            return primary;
        }
        String fallback = lookupUrl(fallbackUrlTemplate, ip);
        return isResolved(fallback) ? fallback : UNKNOWN;
    }

    private String lookupUrl(String template, String ip) {
        try {
            String encodedIp = URLEncoder.encode(ip, StandardCharsets.UTF_8).replace("+", "%20");
            String body = geoLocationClient.get(URI.create(template.formatted(encodedIp)));
            if (body == null || body.isBlank()) {
                return UNKNOWN;
            }
            JsonNode json = objectMapper.readTree(body);
            if (json.path("success").isBoolean() && !json.path("success").asBoolean()) {
                return UNKNOWN;
            }
            String formatted = format(
                    text(json, "city"),
                    firstText(json, "region", "regionName"),
                    firstText(json, "country"),
                    firstText(json, "country_code", "countryCode")
            );
            return formatted.isBlank() ? UNKNOWN : formatted;
        } catch (Exception e) {
            log.warn("Falha ao resolver geolocalização do IP {} | erro={}", ip, e.getMessage());
            return UNKNOWN;
        }
    }

    private static boolean isResolved(String location) {
        return location != null && !location.isBlank() && !UNKNOWN.equals(location) && !INTERNAL.equals(location);
    }

    private static String text(JsonNode json, String field) {
        String value = json.path(field).asText("");
        return value == null ? "" : value.trim();
    }

    private static String firstText(JsonNode json, String... fields) {
        for (String field : fields) {
            String value = text(json, field);
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    static String format(String city, String region, String country) {
        return format(city, region, country, null);
    }

    static String format(String city, String region, String country, String countryCode) {
        String localizedCountry = localizeCountry(country, countryCode);
        Set<String> parts = new LinkedHashSet<>();
        if (notBlank(city)) {
            parts.add(city);
        }
        if (notBlank(region) && !region.equalsIgnoreCase(city)) {
            parts.add(region);
        }
        if (notBlank(localizedCountry)
                && !localizedCountry.equalsIgnoreCase(city)
                && !localizedCountry.equalsIgnoreCase(region)) {
            parts.add(localizedCountry);
        }
        return String.join(", ", parts);
    }

    static String localizeCountry(String country, String countryCode) {
        if ("BR".equalsIgnoreCase(countryCode)
                || "Brazil".equalsIgnoreCase(country)
                || "Brasil".equalsIgnoreCase(country)) {
            return "Brasil";
        }
        return country == null ? "" : country.trim();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
