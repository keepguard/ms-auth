package com.keepguard.ms_auth.infrastructure.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keepguard.ms_auth.application.port.out.geo.GeoLocationPort;
import com.keepguard.ms_auth.infrastructure.util.IpAddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public IpWhoIsGeoLocationAdapter(
            ObjectMapper objectMapper,
            @Value("${app.geo.lookup-url:https://ipwho.is/%s?fields=success,city,region,country}") String lookupUrlTemplate
    ) {
        this.objectMapper = objectMapper;
        this.lookupUrlTemplate = lookupUrlTemplate;
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
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(lookupUrlTemplate.formatted(ip)))
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
                    text(json, "region"),
                    text(json, "country")
            );
            return formatted.isBlank() ? UNKNOWN : formatted;
        } catch (Exception e) {
            log.warn("Falha ao resolver geolocalização do IP {} | erro={}", ip, e.getMessage());
            return UNKNOWN;
        }
    }

    private static String text(JsonNode json, String field) {
        String value = json.path(field).asText("");
        return value == null ? "" : value.trim();
    }

    static String format(String city, String region, String country) {
        Set<String> parts = new LinkedHashSet<>();
        if (notBlank(city)) {
            parts.add(city);
        }
        if (notBlank(region) && !region.equalsIgnoreCase(city)) {
            parts.add(region);
        }
        if (notBlank(country) && !country.equalsIgnoreCase(city) && !country.equalsIgnoreCase(region)) {
            parts.add(country);
        }
        return String.join(", ", parts);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
