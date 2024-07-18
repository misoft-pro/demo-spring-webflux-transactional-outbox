package pro.misoft.payment.component.transaction.backflow

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@Component
class TxAuthResultMap {

    private val log: Logger = LoggerFactory.getLogger(TxAuthResultMap::class.java)

    private val waitingChannels = ConcurrentHashMap<Long, Channel<TxAuthResult>>()

    suspend fun put(key: Long, value: TxAuthResult) {
        waitingChannels.computeIfAbsent(key) { Channel() }.send(value)
        log.info("Result storage for active request is populated with txId=${key} & status=${value.status}")
    }

    suspend fun getWithTimeout(
        key: Long,
        timeout: Long = 2000,
        timeUnit: TimeUnit = TimeUnit.MILLISECONDS
    ): TxAuthResult? {
        val channel = waitingChannels.computeIfAbsent(key) { Channel() }

        val result = withTimeoutOrNull(timeUnit.toMillis(timeout)) {
            channel.receive()
        }
        if (result != null) {
            remove(key)
        }
        return result
    }

    fun remove(key: Long) {
        val element = waitingChannels.remove(key)
        element?.close() // Close the channel when the key is removed
    }
}
