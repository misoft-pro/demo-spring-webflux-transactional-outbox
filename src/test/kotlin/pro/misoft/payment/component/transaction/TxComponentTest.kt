package pro.misoft.payment.component.transaction

import pro.misoft.payment.component.payment.PaymentIntentCreatedEventMother
import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.outbox.Publisher
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pro.misoft.payment.component.transaction.TxComponent


class TxComponentTest {

    private val publisher: Publisher = mockk(relaxUnitFun = true)
    private val txComponent: TxComponent = TxComponent(publisher)

    @Test
    fun `should create tx and process it when risk status is green`() {
        runBlocking {
            val payIntentCreatedEvent =
                PaymentIntentCreatedEventMother.newPaymentIntentCreatedEvent(RiskCheckStatus.GREEN)

            val (txEntity, outboxMessage) = txComponent.processPayment(payIntentCreatedEvent)

            assertThat(txEntity).isNotNull
            assertThat(outboxMessage).isNotNull
            coVerify(exactly = 1) {
                publisher.publish(outboxMessage!!)
            }
        }
    }

    @Test
    fun `should create tx but not process it when risk status is not green`() {
        runBlocking {
            val payIntentCreatedEvent =
                PaymentIntentCreatedEventMother.newPaymentIntentCreatedEvent(RiskCheckStatus.YELLOW)

            val (txEntity, outboxMessage) = txComponent.processPayment(payIntentCreatedEvent)

            assertThat(txEntity).isNotNull
            assertThat(outboxMessage).isNull()
            coVerify(exactly = 0) {
                publisher.publish(
                    any()
                )
            }
        }
    }
}