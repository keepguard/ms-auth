package com.keepguard.ms_auth.infrastructure.config.resilience;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRateLimiterMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedRetryMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedTimeLimiterMetrics;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central de resiliência usando Resilience4j.
 * 
 * <p>Esta classe configura todos os registries necessários para implementar
 * padrões de resiliência em microserviços:
 * 
 * <ul>
 *   <li><b>Circuit Breaker:</b> Previne sobrecarga em cascata quando serviços dependentes falham</li>
 *   <li><b>Retry:</b> Tenta novamente operações que falharam temporariamente</li>
 *   <li><b>Rate Limiter:</b> Limita taxa de requisições para proteger recursos</li>
 *   <li><b>Bulkhead:</b> Isola recursos para evitar esgotamento de threads</li>
 *   <li><b>Time Limiter:</b> Define timeouts para operações assíncronas</li>
 * </ul>
 * 
 * <p>Todas as métricas são automaticamente expostas no endpoint {@code /actuator/prometheus}
 * e podem ser visualizadas no Grafana.
 * 
 * <p><b>Configuração:</b><br>
 * As políticas de resiliência (timeouts, retries, thresholds) são configuradas
 * no arquivo {@code application.yml}. Esta classe apenas cria os registries
 * e registra as métricas no Prometheus.
 * 
 * @author KeepGuard Team
 * @version 1.0.77
 * @since 1.0.77
 * @see io.github.resilience4j.circuitbreaker.CircuitBreaker
 * @see io.github.resilience4j.retry.Retry
 * @see io.github.resilience4j.ratelimiter.RateLimiter
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ResilienceConfig {

    private final MeterRegistry meterRegistry;

    /**
     * Configura o registro de Circuit Breakers com métricas Prometheus.
     * 
     * <p><b>Estados do Circuit Breaker:</b>
     * <ul>
     *   <li><b>CLOSED:</b> Requisições fluindo normalmente</li>
     *   <li><b>OPEN:</b> Muitas falhas detectadas, requisições rejeitadas imediatamente</li>
     *   <li><b>HALF_OPEN:</b> Testando se o serviço voltou ao normal</li>
     * </ul>
     * 
     * <p><b>Métricas exportadas:</b>
     * <ul>
     *   <li>resilience4j_circuitbreaker_state</li>
     *   <li>resilience4j_circuitbreaker_calls_total</li>
     *   <li>resilience4j_circuitbreaker_failure_rate</li>
     * </ul>
     * 
     * @return CircuitBreakerRegistry configurado com métricas Prometheus
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        
        // Registrar métricas no Prometheus
        TaggedCircuitBreakerMetrics
            .ofCircuitBreakerRegistry(registry)
            .bindTo(meterRegistry);
        
        log.info("✅ Circuit Breaker Registry configurado com métricas Prometheus");
        return registry;
    }

    /**
     * Configura o registro de Retry com métricas Prometheus.
     * 
     * <p>Retry implementa tentativas automáticas com backoff exponencial,
     * evitando sobrecarregar serviços que estão se recuperando de falhas.
     * 
     * <p><b>Métricas exportadas:</b>
     * <ul>
     *   <li>resilience4j_retry_calls_total</li>
     * </ul>
     * 
     * @return RetryRegistry configurado com métricas Prometheus
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();
        
        // Registrar métricas no Prometheus
        TaggedRetryMetrics
            .ofRetryRegistry(registry)
            .bindTo(meterRegistry);
        
        log.info("✅ Retry Registry configurado com métricas Prometheus");
        return registry;
    }

    /**
     * Configura o registro de Bulkhead com métricas Prometheus.
     * 
     * <p>Bulkhead isola recursos e limita chamadas concorrentes, prevenindo
     * que um serviço lento ou com falha esgote todos os recursos (threads/conexões).
     * 
     * <p><b>Métricas exportadas:</b>
     * <ul>
     *   <li>resilience4j_bulkhead_available_concurrent_calls</li>
     *   <li>resilience4j_bulkhead_max_allowed_concurrent_calls</li>
     * </ul>
     * 
     * @return BulkheadRegistry configurado com métricas Prometheus
     */
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        BulkheadRegistry registry = BulkheadRegistry.ofDefaults();
        
        // Registrar métricas no Prometheus
        TaggedBulkheadMetrics
            .ofBulkheadRegistry(registry)
            .bindTo(meterRegistry);
        
        log.info("✅ Bulkhead Registry configurado com métricas Prometheus");
        return registry;
    }

    /**
     * Configura o registro de Rate Limiter com métricas Prometheus.
     * 
     * <p>Rate Limiter protege contra sobrecarga limitando o número de requisições
     * em um período de tempo. Essencial para prevenir ataques de força bruta e
     * proteger recursos críticos.
     * 
     * <p><b>Métricas exportadas:</b>
     * <ul>
     *   <li>resilience4j_ratelimiter_available_permissions</li>
     *   <li>resilience4j_ratelimiter_waiting_threads</li>
     * </ul>
     * 
     * @return RateLimiterRegistry configurado com métricas Prometheus
     */
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
        
        // Registrar métricas no Prometheus
        TaggedRateLimiterMetrics
            .ofRateLimiterRegistry(registry)
            .bindTo(meterRegistry);
        
        log.info("✅ Rate Limiter Registry configurado com métricas Prometheus");
        return registry;
    }

    /**
     * Configura o registro de Time Limiter com métricas Prometheus.
     * 
     * <p>Time Limiter define timeouts para operações assíncronas, prevenindo
     * que threads fiquem travadas indefinidamente aguardando respostas.
     * 
     * <p><b>Métricas exportadas:</b>
     * <ul>
     *   <li>resilience4j_timelimiter_calls_total</li>
     * </ul>
     * 
     * @return TimeLimiterRegistry configurado com métricas Prometheus
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();
        
        // Registrar métricas no Prometheus
        TaggedTimeLimiterMetrics
            .ofTimeLimiterRegistry(registry)
            .bindTo(meterRegistry);
        
        log.info("✅ Time Limiter Registry configurado com métricas Prometheus");
        return registry;
    }
}
