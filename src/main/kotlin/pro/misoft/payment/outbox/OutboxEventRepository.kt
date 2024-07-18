package pro.misoft.payment.outbox

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.time.LocalDateTime


@Repository
interface OutboxEventRepository : CoroutineCrudRepository<OutboxEvent, Long>, OutboxEventCustomRepository

interface OutboxEventCustomRepository {

    suspend fun insert(outboxEvent: OutboxEvent): OutboxEvent?
    suspend fun pullAndDeleteStuckOutboxMessages(olderThanMillis: Int = 2000, batchSize: Int = 10): Flow<OutboxEvent>
}

class OutboxEventRepositoryImpl(
    private val template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient,
) : OutboxEventCustomRepository {

    override suspend fun insert(outboxEvent: OutboxEvent): OutboxEvent {
        return template.insert(OutboxEvent::class.java)
            .using(outboxEvent)
            .awaitSingle()
    }

    override suspend fun pullAndDeleteStuckOutboxMessages(olderThanMillis: Int, batchSize: Int): Flow<OutboxEvent> {
        val query = """
            DELETE FROM outbox_events
            WHERE id IN (
                SELECT id 
                FROM outbox_events  
                WHERE created_at < NOW() - INTERVAL '$olderThanMillis milliseconds'
                ORDER BY id   
                FOR UPDATE SKIP LOCKED   
                LIMIT $batchSize 
            ) 
            RETURNING id, class_type, ser_content, created_at;
        """

        val rows = databaseClient.sql(query)
            .map { row, _ ->
                OutboxEvent(
                    id = row.get("id", java.lang.Long::class.java)!!.toLong(),
                    classType = row.get("class_type", String::class.java)!!,
                    serContent = row.get("ser_content", String::class.java)!!,
                    createdAt = row.get("created_at", LocalDateTime::class.java)!!,
                )
            }
            .all()

        return rows.asFlow()
    }
}