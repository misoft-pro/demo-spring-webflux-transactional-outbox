package pro.misoft.payment.outbox

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OutboxEventBatchTask<T>(
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventSender: GrpcEventSender<T>,
    private val callback: suspend (T) -> Unit = {},
    private val olderThanMillis: Int,
    private val batchSize: Int,
) {
    private val log: Logger = LoggerFactory.getLogger(OutboxEventBatchTask::class.java)
    suspend fun run() {
        log.info("Batch task started for olderThanMillis=$olderThanMillis and batchSize=$batchSize")
        val messages = outboxEventRepository.pullAndDeleteStuckOutboxMessages(olderThanMillis, batchSize)
        messages.collect { msg ->
            OutboxEventTask(outboxEventSender, callback).call(msg)
        }
    }
}