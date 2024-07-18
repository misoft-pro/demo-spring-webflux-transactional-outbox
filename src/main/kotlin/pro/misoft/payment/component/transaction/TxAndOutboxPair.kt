package pro.misoft.payment.component.transaction

import pro.misoft.payment.outbox.OutboxEvent

data class TxAndOutboxPair(val txEntity: TxEntity, var outboxEvent: OutboxEvent? = null) {
}