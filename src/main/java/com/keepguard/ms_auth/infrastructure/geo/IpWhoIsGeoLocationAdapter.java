package com.keepguard.ms_auth.infrastructure.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.port.out.geo.GeoLocationPort;
import com.keepguard.ms_auth.infrastructure.util.IpAddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
    private final HttpClient httpClient;
    private final String lookupUrlTemplate;
    private final String fallbackUrlTemplate;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public IpWhoIsGeoLocationAdapter(
            ObjectMapper objectMapper,
            @Value("${app.geo.lookup-url:https://get.geojs.io/v1/ip/geo/%s.json}") String lookupUrlTemplate,
            @Value("${app.geo.fallback-url:https://ipwho.is/%s?fields=success,city,region,country,country_code}") String fallbackUrlTemplate
    ) {
        this.objectMapper = objectMapper;
        this.lookupUrlTemplate = lookupUrlTemplate;
        this.fallbackUrlTemplate = fallbackUrlTemplate;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(2000))
                .build();
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
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(template.formatted(encodedIp)))
                    .timeout(Duration.ofMillis(3000))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null) {
                return UNKNOWN;
            }
            JsonNode json = objectMapper.readTree(response.body());
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
