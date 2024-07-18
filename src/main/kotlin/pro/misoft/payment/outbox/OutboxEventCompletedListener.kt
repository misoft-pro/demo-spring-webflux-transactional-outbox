package pro.misoft.payment.outbox

import pro.misoft.payment.component.transaction.backflow.TxAuthResultMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener


@Component
class OutboxEventCompletedListener(
    private val txAuthResultMap: TxAuthResultMap,
) {
    private val log: Logger = LoggerFactory.getLogger(OutboxEventCompletedListener::class.java)

    @TransactionalEventListener
    suspend fun listen(event: OutboxEventCompletedEvent) {
        log.info("TransactionalEventListener is started for txId=${event.result.txId}")
        txAuthResultMap.put(event.result.txId, event.result)
    }
}