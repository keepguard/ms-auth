package com.keepguard.ms_auth.infrastructure.interceptor;

import com.keepguard.ms_auth.infrastructure.context.CorrelationContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignCorrelationIdInterceptor implements RequestInterceptor {

    private final CorrelationContext correlationContext;

    @Override
    public void apply(RequestTemplate template) {
        String correlationId = correlationContext.getCorrelationId();
        if (correlationId != null && !correlationId.trim().isEmpty()) {
            template.header(CorrelationContext.CORRELATION_ID_HEADER, correlationId);
            log.debug("Adicionando Correlation ID {} na requisição Feign para {}",
                    correlationId, template.url());
        } else {
            log.warn("Correlation ID não encontrado para requisição Feign para {}", template.url());
        }
    }
}