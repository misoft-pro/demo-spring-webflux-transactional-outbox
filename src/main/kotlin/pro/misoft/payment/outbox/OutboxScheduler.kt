package pro.misoft.payment.outbox

import pro.misoft.payment.component.transaction.backflow.TxAuthResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OutboxScheduler(
    private val outboxEventRepository: OutboxEventRepository,
    private val outboxEventSender: GrpcEventSender<TxAuthResult>,
    private val outboxEventCallbackClosure: OutboxEventCallbackClosure,
    @Value("\${pro.misoft.payment.schedulers.outbox.olderThanMillis}")
    private val olderThanMillis: Int = 2000,
    @Value("\${pro.misoft.payment.schedulers.outbox.batchSize}")
    private val batchSize: Int = 10,
) {

    @Scheduled(
        initialDelayString = "\${pro.misoft.payment.schedulers.outbox.initialDelayMillis}",
        fixedRateString = "\${pro.misoft.payment.schedulers.outbox.fixedRate}"
    )
    @Transactional
    suspend fun publishAndDeleteOutboxRecords() {
        OutboxEventBatchTask(
            outboxEventRepository,
            outboxEventSender,
            outboxEventCallbackClosure.callback,
            olderThanMillis,
            batchSize
        )
            .run()
    }
}