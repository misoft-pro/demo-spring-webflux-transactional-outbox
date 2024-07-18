package pro.misoft.payment.component.transaction

import pro.misoft.payment.common.IdGenerator
import pro.misoft.payment.component.payment.PaymentIntentCreatedEvent
import pro.misoft.payment.component.risk.RiskCheckStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Table("transactions")
data class TxEntity(
    @Id val id: Long = IdGenerator.uniqueId(),
    val requestId: String,
    val paymentIntentId: Long,
    var acquirerTxId: String? = null,
    var acquirerId: String? = null,
    var challengeUrl: String? = null,
    val merchantId: String,
    val merchantRedirectUrl: String,
    val cardNumber: String,
    val cardExpiryDate: LocalDate,
    val cardToken: String,
    val orderId: String,
    val orderAmount: BigDecimal,
    val orderCurrency: String,
    var riskCheckStatus: RiskCheckStatus = RiskCheckStatus.NEW,
    var transactionStatus: TxStatus = TxStatus.NEW,
    @CreatedDate var createdDate: LocalDateTime? = LocalDateTime.now(ZoneId.of("UTC")),
    @LastModifiedDate var lastModifiedDate: LocalDateTime? = LocalDateTime.now(ZoneId.of("UTC"))
) {
    constructor(event: PaymentIntentCreatedEvent) : this(
        id = IdGenerator.uniqueId(),
        requestId = event.requestId,
        paymentIntentId = event.intentId,
        merchantId = event.merchantId,
        merchantRedirectUrl = event.merchantRedirectUrl,
        cardNumber = event.cardNumber,
        cardExpiryDate = event.cardExpiryDate,
        cardToken = event.cardToken,
        orderId = event.orderId,
        orderAmount = event.orderAmount,
        orderCurrency = event.orderCurrency,
        riskCheckStatus = event.riskCheckStatus,
        transactionStatus = TxStatus.NEW,
    )
}