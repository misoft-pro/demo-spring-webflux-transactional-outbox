package pro.misoft.payment.component.risk

import pro.misoft.payment.web.api.PaymentRequest


interface RiskCheckService {
    fun performRiskCheck(entity: PaymentRequest): RiskCheckStatus
}