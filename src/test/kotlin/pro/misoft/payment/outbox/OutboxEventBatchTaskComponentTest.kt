package pro.misoft.payment.outbox

import pro.misoft.payment.TestConfigApiServerApplication
import pro.misoft.payment.component.transaction.TxEntityMother
import pro.misoft.payment.component.transaction.TxRepository
import pro.misoft.payment.component.transaction.TxStatus
import pro.misoft.payment.component.transaction.backflow.TxAuthResult
import pro.misoft.payment.component.transaction.backflow.TxAuthResultMother
import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestConfigApiServerApplication::class)
class OutboxEventBatchTaskComponentTest {

    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var omc: OutboxEventCallbackClosure

    @Autowired
    private lateinit var txRepository: TxRepository

    @MockkBean
    private lateinit var outboxMessageSender: GrpcEventSender<TxAuthResult>

    @Test
    fun `should run batch task to process stuck outbox messages`() {
        runBlocking {
            val txEntity = TxEntityMother.newTxEntity()
            txRepository.insert(txEntity)
            val outboxMessage = OutboxEventMother.newOutboxEvent(txEntity.id)
            outboxEventRepository.insert(outboxMessage)
            coEvery { outboxMessageSender.sendEvent(any()) } returns TxAuthResultMother.newTxAuthResult(
                txEntity.id
            )
            val batchTask = OutboxEventBatchTask(outboxEventRepository, outboxMessageSender, omc.callback, 1, 1)

            batchTask.run()

            assertThat(outboxEventRepository.findById(outboxMessage.id)).isNull()
            val updatedTx = txRepository.findById(txEntity.id)
            assertThat(updatedTx).isNotNull()
            assertThat(updatedTx?.transactionStatus).isEqualTo(TxStatus.CUSTOMER_VERIFICATION)

            coVerify { outboxMessageSender.sendEvent(any()) }
        }
    }
}