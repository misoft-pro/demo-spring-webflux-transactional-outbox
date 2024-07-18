package pro.misoft.payment.outbox

import pro.misoft.payment.component.transaction.backflow.TxAuthResult
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.reactive.TransactionalEventPublisher

@Component
class OutboxEventProcessor(
    private val outboxEventSender: GrpcEventSender<TxAuthResult>,
    private val omc: OutboxEventCallbackClosure,
    private val outboxEventRepository: OutboxEventRepository,
    private val transactionalEventPublisher: TransactionalEventPublisher
) {
    private val log: Logger = LoggerFactory.getLogger(OutboxEventProcessor::class.java)

    @Transactional
    suspend fun processOutboxEvent(event: OutboxEvent) {
        log.info("OutboxMessageTask started for txId=${event.id}")
        val txAuthResult = OutboxEventTask(outboxEventSender, omc.callback).call(event)
        outboxEventRepository.deleteById(event.id)

        transactionalEventPublisher.publishEvent { txContext -> OutboxEventCompletedEvent(txContext, txAuthResult) }
            .awaitSingleOrNull()
    }
}