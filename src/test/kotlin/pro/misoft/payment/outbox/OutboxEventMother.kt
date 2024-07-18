package pro.misoft.payment.outbox

import pro.misoft.payment.common.IdGenerator.uniqueId
import pro.misoft.payment.component.transaction.TxEntity

object OutboxEventMother {

    fun newOutboxEvent(id: Long = uniqueId()): OutboxEvent {
        return OutboxEvent(
            id,
            TxEntity::class.java.name,
            "serializedContent"
        )
    }
}