package pro.misoft.payment.component.acquirer

import pro.misoft.payment.component.transaction.TxStatus
import pro.misoft.payment.component.transaction.backflow.TxAuthResult
import pro.misoft.payment.outbox.GrpcEventSender
import pro.misoft.payment.outbox.OutboxEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AcquirerClient : GrpcEventSender<TxAuthResult>() {

    private val log: Logger = LoggerFactory.getLogger(AcquirerClient::class.java)

    override suspend fun sendEvent(event: OutboxEvent): TxAuthResult {
        //TODO use gRPC client to send transaction to Acquirer and return response
        val result = TxAuthResult(
            event.id,
            "acquirerTxId123",
            "https://challengeUrl",
            TxStatus.CUSTOMER_VERIFICATION.label
        )
        log.info("Message with txId=${event.id} is send to external acquirer")
        return result
    }
}