package pro.misoft.payment.component.transaction.backflow

interface TxAuthResultPoller {
    suspend fun pollTxAuthResult(txId: Long): TxAuthResult
}