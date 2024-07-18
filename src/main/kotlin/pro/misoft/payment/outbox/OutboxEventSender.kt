package pro.misoft.payment.outbox

interface OutboxEventSender<T> {

    suspend fun sendEvent(event: OutboxEvent): T
}