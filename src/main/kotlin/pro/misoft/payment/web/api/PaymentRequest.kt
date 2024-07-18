package pro.misoft.payment.web.api

import pro.misoft.payment.component.payment.PaymentIntent
import pro.misoft.payment.component.payment.PaymentIntentCreatedEvent
import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.config.Mapper
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.r2dbc.postgresql.codec.Json
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.YearMonth

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentRequest(
    @JsonProperty("requestId")  @field:NotBlank val requestId: String,
    @JsonProperty("merchantId") @field:NotBlank val merchantId: String,
    @JsonProperty("merchantRedirectUrl") @field:NotBlank val merchantRedirectUrl: String,
    @JsonProperty("card") @field:NotNull val card: Card,
    @JsonProperty("order") @field:NotNull val order: Order
) {

    fun toEntity(): PaymentIntent {
        return PaymentIntent(
            requestId = requestId,
            originalRequest = Json.of(Mapper.writeValueAsString(this))
        )
    }

    fun toPaymentIntentCreatedEvent(paymentId: Long, riskCheckStatus: RiskCheckStatus): PaymentIntentCreatedEvent {
        return PaymentIntentCreatedEvent(
            requestId = requestId,
            intentId = paymentId,
            merchantId = merchantId,
            merchantRedirectUrl = merchantRedirectUrl,
            cardNumber = card.number,
            cardExpiryDate = card.expiryDate.atEndOfMonth(),
            cardToken = card.cardToken,
            orderId = order.id,
            orderCurrency = order.total.currency,
            orderAmount = BigDecimal(order.total.amount),
            riskCheckStatus = riskCheckStatus
        )
    }

    data class Card(
        @field:NotBlank val number: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/yy")
        @Schema(type = "string", format = "month/year", example = "12/25", description = "Month and year in MM/yy")
        @field:NotNull val expiryDate: YearMonth,
        @field:NotBlank val cardToken: String
    ) {
    }

    data class Order(val id: String, val total: Total) {
    }

    data class Total(val amount: String, val currency: String) {
    }
}
