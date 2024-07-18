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
class OutboxEventProcessorTest {

    @Autowired
    private lateinit var outboxEventProcessor: OutboxEventProcessor

    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var txRepository: TxRepository

    @MockkBean
    private lateinit var outboxEventSender: GrpcEventSender<TxAuthResult>

    @MockkBean
    private lateinit var outboxEventCompletedListener: OutboxEventCompletedListener


    @Test
    fun `after outbox event is processed it should update tx entity status and be deleted from db`() {
        runBlocking {
            //given
            val txEntity = TxEntityMother.newTxEntity()
            txRepository.insert(txEntity)
            val outboxMessage = OutboxEventMother.newOutboxEvent(txEntity.id)
            outboxEventRepository.insert(outboxMessage)
            coEvery { outboxEventSender.sendEvent(outboxMessage) } returns TxAuthResultMother.newTxAuthResult(
                txEntity.id
            )

            //when
            outboxEventProcessor.processOutboxEvent(outboxMessage)

            //then
            assertThat(outboxEventRepository.findById(outboxMessage.id)).isNull()
            val updatedTx = txRepository.findById(txEntity.id)
            assertThat(updatedTx).isNotNull()
            assertThat(updatedTx?.transactionStatus).isEqualTo(TxStatus.CUSTOMER_VERIFICATION)

            coVerify { outboxEventSender.sendEvent(outboxMessage) }
        }
    }

    @Test
    fun `OutboxEventCompletedEvent processing should run only after active tx commit`() {
        runBlocking {
            //given
            val txEntity = TxEntityMother.newTxEntity()
            txRepository.insert(txEntity)
            val outboxMessage = OutboxEventMother.newOutboxEvent(txEntity.id)
            outboxEventRepository.insert(outboxMessage)
            coEvery { outboxEventSender.sendEvent(outboxMessage) } returns TxAuthResultMother.newTxAuthResult(
                txEntity.id
            )
            coEvery { outboxEventCompletedListener.listen(any()) } throws RuntimeException("listener fails for any reason")

            //when
            outboxEventProcessor.processOutboxEvent(outboxMessage)

            //then
            assertThat(outboxEventRepository.findById(outboxMessage.id)).isNull()
            val updatedTx = txRepository.findById(txEntity.id)
            assertThat(updatedTx).isNotNull()
            assertThat(updatedTx?.transactionStatus).isEqualTo(TxStatus.CUSTOMER_VERIFICATION)
            coVerify { outboxEventSender.sendEvent(outboxMessage) }
        }
    }
}