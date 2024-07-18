package pro.misoft.payment.outbox

import pro.misoft.payment.component.transaction.TxRepository
import pro.misoft.payment.component.transaction.TxStatus
import pro.misoft.payment.component.transaction.backflow.TxAuthResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class OutboxEventCallbackClosure(
    private val txRepository: TxRepository
) {
    private val log = LoggerFactory.getLogger(OutboxEventCallbackClosure::class.java)

    val callback: suspend (TxAuthResult) -> Unit = { result ->
        txRepository.updateByAuthResult(
            result.txId,
            TxStatus.of(result.status),
            result.acquirerTxId,
            result.challengeUrl
        )
        log.info("Callback with tx result is completed for txId=${result.txId}")
    }
}