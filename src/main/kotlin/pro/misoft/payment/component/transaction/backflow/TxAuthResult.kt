package pro.misoft.payment.component.transaction.backflow

data class TxAuthResult(
    val txId: Long,
    val acquirerTxId: String,
    var challengeUrl: String,
    val status: String
) {
}