package pro.misoft.payment.outbox;

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation.MANDATORY
import org.springframework.transaction.annotation.Transactional

@Component
class TxOutboxPublisher(
    private val outboxEventRepository: OutboxEventRepository,
) : Publisher {

    @Transactional(propagation = MANDATORY)
    override suspend fun publish(event: OutboxEvent) {
        outboxEventRepository.insert(event)
    }
}
