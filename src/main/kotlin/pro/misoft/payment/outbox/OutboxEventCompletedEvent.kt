package pro.misoft.payment.outbox

import pro.misoft.payment.component.transaction.backflow.TxAuthResult
import org.springframework.context.ApplicationEvent
import org.springframework.transaction.reactive.TransactionContext

data class OutboxEventCompletedEvent(val txContext: TransactionContext, val result: TxAuthResult) : ApplicationEvent(
    txContext
)
