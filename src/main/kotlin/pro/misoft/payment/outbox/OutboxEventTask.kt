package pro.misoft.payment.outbox

class OutboxEventTask<T>(
    private val outboxEventSender: GrpcEventSender<T>,
    private val callback: suspend (T) -> Unit = {},
) {
    suspend fun call(outboxEvent: OutboxEvent): T {
        val result = outboxEventSender.sendEvent(outboxEvent)
        callback(result)
        return result
    }
}