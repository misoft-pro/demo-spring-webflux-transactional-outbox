package pro.misoft.payment.web.api

import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.component.transaction.TxStatus
import pro.misoft.payment.component.transaction.backflow.TxAuthResult
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentResponse(
    @JsonProperty("txId") val txId: String,
    @JsonProperty("riskCheckStatus") val riskCheckStatus: RiskCheckStatus,
    @JsonProperty("transactionStatus") var transactionStatus: TxStatus,
    @JsonProperty("challengeUrl") var challengeUrl: String?,
) {
    fun update(result: TxAuthResult) {
        this.challengeUrl = result.challengeUrl
        this.transactionStatus = TxStatus.of(result.status)
    }

    companion object {

        fun of(txId: String, txStatus: TxStatus, riskCheckStatus: RiskCheckStatus): PaymentResponse {
            return PaymentResponse(
                txId,
                riskCheckStatus,
                txStatus,
                null
            )
        }
    }
}