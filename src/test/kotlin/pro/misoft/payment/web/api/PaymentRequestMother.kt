package pro.misoft.payment.web.api

import pro.misoft.payment.common.IdGenerator
import java.time.YearMonth

object PaymentRequestMother {

    fun newPaymentRequest(): PaymentRequest {
        return PaymentRequest(
            requestId = "requestId-${IdGenerator.uniqueIdString()}",
            merchantId = "customMerchantId",
            merchantRedirectUrl = "https://merchant.com/search?p=magnis",
            card = PaymentRequest.Card(
                number = "123456******5678",
                expiryDate = YearMonth.now(),
                cardToken = IdGenerator.uniqueIdString()
            ),
            order = PaymentRequest.Order(
                id = "orderId-${IdGenerator.uniqueIdString()}",
                total = PaymentRequest.Total(amount = "100", currency = "EUR")
            )
        )
    }
}
