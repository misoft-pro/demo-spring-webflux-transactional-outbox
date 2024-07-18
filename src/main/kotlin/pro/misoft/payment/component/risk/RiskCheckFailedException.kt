package pro.misoft.payment.component.risk

import pro.misoft.payment.common.BusinessException

class RiskCheckFailedException(errorMsg: String) : BusinessException(errorMsg) {
}
