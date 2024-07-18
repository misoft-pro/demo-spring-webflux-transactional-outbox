package pro.misoft.payment.component.transaction.backflow

import pro.misoft.payment.common.IdGenerator
import pro.misoft.payment.component.transaction.TxStatus
import pro.misoft.payment.component.transaction.backflow.TxAuthResult

object TxAuthResultMother {
    fun newTxAuthResult(txId: Long, txStatus: TxStatus = TxStatus.CUSTOMER_VERIFICATION): TxAuthResult {
        return TxAuthResult(txId, IdGenerator.uniqueIdString(), "challengeUrl", txStatus.label)
    }
}