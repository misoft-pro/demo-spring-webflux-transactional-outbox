package pro.misoft.payment.outbox

interface Publisher {

    suspend fun publish(event: OutboxEvent)
}