package com.keepguard.ms_auth.application.port.out.nosql;

import java.util.List;
import java.util.Optional;

/**
 * Port para repositórios NoSQL (MongoDB, Cassandra, etc).
 * 
 * <p>Esta interface genérica define operações básicas para persistência em
 * bancos de dados NoSQL. A implementação pode usar MongoDB, Cassandra,
 * DynamoDB ou qualquer outro banco NoSQL.
 * 
 * <p><b>Implementação Futura (MongoDB):</b><br>
 * Quando MongoDB for adicionado ao projeto, criar:
 * <ul>
 *   <li>{@code MongoDBUserRepositoryAdapter} em {@code infrastructure/nosql/mongodb/}</li>
 *   <li>Aplicar Circuit Breaker, Retry e Timeout</li>
 *   <li>Configurar no {@code application.yml}</li>
 * </ul>
 * 
 * <p><b>Casos de Uso:</b>
 * <ul>
 *   <li>Armazenar logs de auditoria</li>
 *   <li>Armazenar eventos históricos</li>
 *   <li>Queries analíticas (CQRS - Read Model)</li>
 * </ul>
 * 
 * @param <T> Tipo da entidade
 * @param <ID> Tipo do identificador
 * @author KeepGuard Team
 * @version 1.0.77
 * @since 1.0.77
 */
public interface NoSQLRepositoryPort<T, ID> {
    
    /**
     * Salva uma entidade no banco NoSQL.
     * 
     * @param entity Entidade a ser salva
     * @return Entidade salva (pode incluir ID gerado)
     */
    T save(T entity);
    
    /**
     * Busca uma entidade por ID.
     * 
     * @param id Identificador da entidade
     * @return Optional com a entidade ou empty
     */
    Optional<T> findById(ID id);
    
    /**
     * Busca todas as entidades.
     * 
     * @return Lista de entidades
     */
    List<T> findAll();
    
    /**
     * Remove uma entidade por ID.
     * 
     * @param id Identificador da entidade
     */
    void deleteById(ID id);
    
    /**
     * Verifica se existe uma entidade com o ID.
     * 
     * @param id Identificador da entidade
     * @return true se existe, false caso contrário
     */
    boolean existsById(ID id);
}
