package com.keepguard.ms_auth.adapters.out.feign;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Adapter resiliente para comunicação com ms-communication via Feign.
 * 
 * <p>Este adapter encapsula todas as chamadas ao {@link CommunicationClient},
 * aplicando os seguintes padrões de resiliência:
 * 
 * <ul>
 *   <li><b>Circuit Breaker:</b> Abre após 50% de falhas em 10 chamadas, aguarda 30s antes de tentar novamente</li>
 *   <li><b>Retry:</b> 3 tentativas com backoff exponencial (2s, 4s, 8s)</li>
 *   <li><b>Bulkhead:</b> Máximo de 10 chamadas simultâneas</li>
 *   <li><b>Time Limiter:</b> Timeout de 5 segundos por chamada</li>
 * </ul>
 * 
 * <p><b>Graceful Degradation:</b><br>
 * Quando todas as tentativas falham ou o Circuit Breaker está OPEN, os métodos de
 * fallback são executados, retornando respostas padrão que permitem que o ms-auth
 * continue operando mesmo sem o ms-communication disponível.
 * 
 * <p><b>Observabilidade:</b><br>
 * Todos os eventos são logados com níveis apropriados (INFO para sucesso, WARN/ERROR para falhas)
 * e métricas são expostas no endpoint {@code /actuator/prometheus}.
 * 
 * <p><b>Exemplo de uso:</b>
 * <pre>{@code
 * CompletableFuture<String> health = adapter.checkHealth("ms-auth");
 * String result = health.get(); // Aguarda resultado assíncrono
 * }</pre>
 * 
 * @author KeepGuard Team
 * @version 1.0.77
 * @since 1.0.77
 * @see CommunicationClient
 * @see io.github.resilience4j.circuitbreaker.CircuitBreaker
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunicationClientAdapter {

    private final CommunicationClient communicationClient;

    /**
     * Verifica o health do ms-communication com resiliência completa.
     * 
     * <p><b>Padrões aplicados:</b>
     * <ul>
     *   <li>Circuit Breaker: Abre após 50% de falhas</li>
     *   <li>Retry: 3 tentativas com backoff exponencial (2s, 4s, 8s)</li>
     *   <li>Bulkhead: Máximo 10 chamadas simultâneas</li>
     *   <li>Timeout: 5 segundos</li>
     * </ul>
     * 
     * <p><b>Fallback:</b><br>
     * Quando todas as tentativas falham, retorna {@code "CIRCUIT_OPEN"},
     * indicando que o serviço está temporariamente indisponível.
     * 
     * @param application Nome da aplicação que está chamando (para identificação)
     * @return CompletableFuture com resposta do health check ou fallback
     */
    @CircuitBreaker(name = "communicationClient", fallbackMethod = "healthFallback")
    @Retry(name = "communicationClient")
    @Bulkhead(name = "communicationClient")
    @TimeLimiter(name = "communicationClient")
    public CompletableFuture<String> checkHealth(String application) {
        log.debug("Verificando health do ms-communication | application={}", application);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = communicationClient.health(application);
                log.debug("Health check ms-communication OK | response={}", response);
                return response;
            } catch (Exception ex) {
                log.error("Erro ao verificar health do ms-communication: {}", 
                    ex.getMessage(), ex);
                throw ex;
            }
        });
    }

    /**
     * Testa a comunicação com ms-communication com resiliência completa.
     * 
     * <p><b>Padrões aplicados:</b>
     * <ul>
     *   <li>Circuit Breaker: Abre após 50% de falhas</li>
     *   <li>Retry: 3 tentativas com backoff exponencial</li>
     *   <li>Bulkhead: Máximo 10 chamadas simultâneas</li>
     *   <li>Timeout: 5 segundos</li>
     * </ul>
     * 
     * <p><b>Fallback:</b><br>
     * Quando todas as tentativas falham, retorna {@code "SERVICE_UNAVAILABLE"},
     * permitindo que a aplicação trate a indisponibilidade gracefully.
     * 
     * @param application Nome da aplicação que está chamando
     * @return CompletableFuture com resposta do teste ou fallback
     */
    @CircuitBreaker(name = "communicationClient", fallbackMethod = "testCommunicationFallback")
    @Retry(name = "communicationClient")
    @Bulkhead(name = "communicationClient")
    @TimeLimiter(name = "communicationClient")
    public CompletableFuture<String> testCommunication(String application) {
        log.debug("Testando comunicacao com ms-communication | application={}", application);
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = communicationClient.testCommunication(application);
                log.debug("Teste de comunicacao OK | response={}", response);
                return response;
            } catch (Exception ex) {
                log.error("Erro ao testar comunicação com ms-communication: {}", 
                    ex.getMessage(), ex);
                throw ex;
            }
        });
    }

    // ========================================================================
    // Fallback Methods - Graceful Degradation
    // ========================================================================

    /**
     * Fallback executado quando o Circuit Breaker está OPEN ou quando
     * todas as tentativas de retry falharam no health check.
     * 
     * <p><b>Comportamento:</b><br>
     * Retorna {@code "CIRCUIT_OPEN"} indicando que o ms-communication está
     * temporariamente indisponível. A aplicação pode continuar operando
     * em modo degradado.
     * 
     * <p><b>Logging:</b><br>
     * Loga erro com informações do contexto (application e erro) para
     * facilitar debugging em produção.
     * 
     * @param application Nome da aplicação que estava chamando
     * @param ex Exceção que causou a ativação do fallback
     * @return CompletableFuture com resposta padrão "CIRCUIT_OPEN"
     */
    private CompletableFuture<String> healthFallback(String application, Exception ex) {
        log.error("FALLBACK: ms-communication indisponivel para health check | " +
                "application={} | erro={} | mensagem={}", 
            application, 
            ex.getClass().getSimpleName(),
            ex.getMessage());
        
        return CompletableFuture.completedFuture("CIRCUIT_OPEN");
    }

    /**
     * Fallback executado quando o Circuit Breaker está OPEN ou quando
     * todas as tentativas de retry falharam no teste de comunicação.
     * 
     * <p><b>Comportamento:</b><br>
     * Retorna {@code "SERVICE_UNAVAILABLE"} indicando que o ms-communication
     * está temporariamente indisponível. A aplicação pode tratar isso
     * apropriadamente (ex: retornar erro 503 ao usuário).
     * 
     * <p><b>Logging:</b><br>
     * Loga erro com informações do contexto para facilitar debugging.
     * 
     * @param application Nome da aplicação que estava chamando
     * @param ex Exceção que causou a ativação do fallback
     * @return CompletableFuture com resposta padrão "SERVICE_UNAVAILABLE"
     */
    private CompletableFuture<String> testCommunicationFallback(String application, Exception ex) {
        log.error("FALLBACK: Teste de comunicacao falhou | " +
                "application={} | erro={} | mensagem={}", 
            application,
            ex.getClass().getSimpleName(),
            ex.getMessage());
        
        return CompletableFuture.completedFuture("SERVICE_UNAVAILABLE");
    }
}
