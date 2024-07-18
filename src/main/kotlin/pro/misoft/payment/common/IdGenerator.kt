package pro.misoft.payment.common

import io.hypersistence.tsid.TSID
import kotlin.math.ln

/**
 * ID generator generate Time Sorted ID (TSID) which is only 64-bit unique number and takes 2times less space than UUID.
 * Every next number is time sorted which avoid page swapping on storage layer because every next number is bigger than previous.
 *
 * To understand why TSID is the best choice for DB persistence see article below
 * @see <a href="https://vladmihalcea.com/uuid-database-primary-key">known article</a>
 */
object IdGenerator {
    private const val TSID_NODE_COUNT_PROPERTY: String = "tsid.node.count"
    private const val TSID_NODE_COUNT_ENV: String = "TSID_NODE_COUNT"
    private const val DEFAULT_NODE_COUNT = 32

    private var TSID_FACTORY: TSID.Factory

    init {
        var nodeCountSetting = System.getProperty(
            TSID_NODE_COUNT_PROPERTY
        )
        if (nodeCountSetting == null) {
            nodeCountSetting = System.getenv(
                TSID_NODE_COUNT_ENV
            )
        }

        val nodeCount = nodeCountSetting?.toInt() ?: DEFAULT_NODE_COUNT

        TSID_FACTORY = newTsidFactory(nodeCount)
    }

    fun uniqueId(): Long {
        return TSID_FACTORY.generate().toLong()
    }

    fun uniqueIdString(): String {
        return TSID_FACTORY.generate().toString()
    }

    private fun newTsidFactory(nodeCount: Int): TSID.Factory {
        val nodeBits = (ln(nodeCount.toDouble()) / ln(2.0)).toInt()

        return TSID.Factory.builder()
            .withRandomFunction(
                TSID.Factory.THREAD_LOCAL_RANDOM_FUNCTION
            )
            .withNodeBits(nodeBits)
            .build()
    }
}
