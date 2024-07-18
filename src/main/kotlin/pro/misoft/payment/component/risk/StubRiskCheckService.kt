package pro.misoft.payment.component.risk

import org.springframework.stereotype.Component
import pro.misoft.payment.web.api.PaymentRequest

@Component
class StubRiskCheckService : RiskCheckService {
    override fun performRiskCheck(entity: PaymentRequest): RiskCheckStatus {
        //perform actual external check
        return RiskCheckStatus.GREEN
    }
}