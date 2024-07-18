package pro.misoft.payment.component.transaction.backflow

import pro.misoft.payment.common.DownstreamTimeoutException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class LocalTxAuthResultPoller(
    private val payAuthResultMap: TxAuthResultMap,
    @Value("\${pro.misoft.payment.poller.timeoutMillis}") private val timeoutMillis: Long = 2000
) : TxAuthResultPoller {

    private val log: Logger = LoggerFactory.getLogger(LocalTxAuthResultPoller::class.java)

    override suspend fun pollTxAuthResult(txId: Long): TxAuthResult {
        log.info("Polling is started for txId=$txId")
        val result = (payAuthResultMap.getWithTimeout(txId, timeoutMillis)
            ?: throw DownstreamTimeoutException("Payment authentication timed out for txId=$txId"))
        return result
    }
}

