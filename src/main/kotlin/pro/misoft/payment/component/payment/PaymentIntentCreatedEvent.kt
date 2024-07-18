package pro.misoft.payment.component.payment

import pro.misoft.payment.component.risk.RiskCheckStatus
import java.math.BigDecimal
import java.time.LocalDate

class PaymentIntentCreatedEvent(
    val requestId: String,
    val intentId: Long,
    val merchantId: String,
    val merchantRedirectUrl: String,
    val cardNumber: String,
    val cardExpiryDate: LocalDate,
    val cardToken: String,
    val orderId: String,
    val orderAmount: BigDecimal,
    val orderCurrency: String,
    val riskCheckStatus: RiskCheckStatus
)
