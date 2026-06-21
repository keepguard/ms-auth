package com.keepguard.ms_auth.application.port.out.messaging;

/**
 * Port para publicação de eventos em sistema de mensageria (RabbitMQ, etc).
 * 
 * <p>Esta interface define o contrato para publicação de eventos de domínio
 * em um sistema de mensageria. A implementação pode usar RabbitMQ ou
 * qualquer outro message broker.
 * 
 * <p><b>Eventos de Domínio (exemplos):</b>
 * <ul>
 *   <li>UserCreatedEvent</li>
 *   <li>UserAuthenticatedEvent</li>
 *   <li>UserBlockedEvent</li>
 *   <li>PasswordChangedEvent</li>
 * </ul>
 * 
 * @author KeepGuard Team
 * @version 1.0.77
 * @since 1.0.77
 */
public interface EventPublisherPort {
    
    /**
     * Publica um evento de domínio no message broker.
     * 
     * @param topic Tópico/fila onde o evento será publicado
     * @param event Evento a ser publicado (será serializado como JSON)
     * @param <T> Tipo do evento
     */
    <T> void publishEvent(String topic, T event);
    
    /**
     * Publica um evento de domínio com chave (para particionamento).
     * 
     * @param topic Tópico/fila onde o evento será publicado
     * @param key Chave para particionamento (ex: userId, companyId)
     * @param event Evento a ser publicado
     * @param <T> Tipo do evento
     */
    <T> void publishEventWithKey(String topic, String key, T event);
}
