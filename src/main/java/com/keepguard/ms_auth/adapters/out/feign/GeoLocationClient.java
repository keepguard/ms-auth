package com.keepguard.ms_auth.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

@FeignClient(
        name = "geo-location",
        url = "https://get.geojs.io",
        configuration = GeoLocationClientConfig.class
)
public interface GeoLocationClient {

    @GetMapping
    String get(URI uri);
}
