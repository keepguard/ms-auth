package com.keepguard.ms_auth.infrastructure.interceptor;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Erro na requisição Feign - Method: {}, Status: {}, Reason: {}",
                methodKey, response.status(), response.reason());

        return FeignException.errorStatus(methodKey, response);
    }
}