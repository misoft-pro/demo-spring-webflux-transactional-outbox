package pro.misoft.payment.outbox

open class GrpcEventSender<T> : OutboxEventSender<T> {

    override suspend fun sendEvent(event: OutboxEvent): T {
        TODO("implement gRPC call")
    }
}