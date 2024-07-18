package pro.misoft.payment.component.transaction

import pro.misoft.payment.component.payment.PaymentIntentCreatedEvent
import pro.misoft.payment.component.risk.RiskCheckStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import pro.misoft.payment.component.transaction.TxEntity
import pro.misoft.payment.component.transaction.TxStatus
import java.math.BigDecimal
import java.time.LocalDate

class TxEntityTest {

    @Test
    fun `should create transaction from PaymentIntentCreatedEvent`() {
        // Create a sample PaymentIntentCreatedEvent
        val event = PaymentIntentCreatedEvent(
            requestId = "req123",
            intentId = 1L,
            merchantId = "merchant123",
            merchantRedirectUrl = "http://merchant.com/redirect",
            cardNumber = "4111111111111111",
            cardExpiryDate = LocalDate.of(2025, 12, 31),
            cardToken = "token123",
            orderId = "order123",
            orderAmount = BigDecimal.valueOf(100.00),
            orderCurrency = "USD",
            riskCheckStatus = RiskCheckStatus.NEW
        )

        // Create an instance of TransactionEntity using the secondary constructor
        val transaction = TxEntity(event)

        // Verify that the properties are correctly initialized using AssertJ's assertThat
        assertThat(transaction.requestId).isEqualTo(event.requestId)
        assertThat(transaction.paymentIntentId).isEqualTo(event.intentId)
        assertThat(transaction.merchantId).isEqualTo(event.merchantId)
        assertThat(transaction.merchantRedirectUrl).isEqualTo(event.merchantRedirectUrl)
        assertThat(transaction.cardNumber).isEqualTo(event.cardNumber)
        assertThat(transaction.cardExpiryDate).isEqualTo(event.cardExpiryDate)
        assertThat(transaction.cardToken).isEqualTo(event.cardToken)
        assertThat(transaction.orderId).isEqualTo(event.orderId)
        assertThat(transaction.orderAmount).isEqualByComparingTo(event.orderAmount)
        assertThat(transaction.orderCurrency).isEqualTo(event.orderCurrency)
        assertThat(transaction.riskCheckStatus).isEqualTo(event.riskCheckStatus)
        assertThat(transaction.transactionStatus).isEqualTo(TxStatus.NEW)
    }
}
