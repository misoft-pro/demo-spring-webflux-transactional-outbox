package pro.misoft.payment.outbox

import pro.misoft.payment.TestConfigApiServerApplication
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@SpringBootTest
@Import(TestConfigApiServerApplication::class)
class TxOutboxPublisherTest {

    @Autowired
    private lateinit var publisher: TxOutboxPublisher

    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var transactionalOperator: TransactionalOperator

    @Test
    fun `should fail to call publish when no active transaction present`() {
        assertThatThrownBy {
            runBlocking {
                publisher.publish(OutboxEventMother.newOutboxEvent())
            }
        }.isInstanceOf(
            IllegalTransactionStateException::class.java
        ).hasMessage("No existing transaction found for transaction marked with propagation 'mandatory'")
    }

    @Test
    fun `should publish successfully and found outbox record in DB`() {
        runBlocking {
            val outMsg = OutboxEventMother.newOutboxEvent()
            transactionalOperator.executeAndAwait {
                publisher.publish(outMsg)
            }
            assertThat(outboxEventRepository.findById(outMsg.id)).isNotNull()
        }
    }
}

