package pro.misoft.payment.component.payment

import pro.misoft.payment.common.IdGenerator
import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.web.api.PaymentRequestMother
import pro.misoft.payment.component.payment.PaymentIntentCreatedEvent

object PaymentIntentCreatedEventMother {

    fun newPaymentIntentCreatedEvent(riskCheckStatus: RiskCheckStatus = RiskCheckStatus.GREEN): PaymentIntentCreatedEvent {
        return PaymentRequestMother.newPaymentRequest()
            .toPaymentIntentCreatedEvent(IdGenerator.uniqueId(), riskCheckStatus)
    }
}