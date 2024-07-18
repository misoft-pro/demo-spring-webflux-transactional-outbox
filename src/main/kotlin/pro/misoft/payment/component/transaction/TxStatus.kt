package pro.misoft.payment.component.transaction

enum class TxStatus(val label: String) {
    NEW("NEW"),
    PENDING("PENDING"),
    FAILED("FAILED"),
    REFUNDED("REFUNDED"),
    RISK_CHECK_PASSED("RISK_CHECK_PASSED"),
    RISK_CHECK_FAILED("RISK_CHECK_FAILED"),
    CUSTOMER_VERIFICATION("CUSTOMER_VERIFICATION"),
    SUCCEED("SUCCEED");

    companion object {
        public fun of(label: String): TxStatus = when (label) {
            "NEW" -> NEW
            "PENDING" -> PENDING
            "FAILED" -> FAILED
            "REFUNDED" -> REFUNDED
            "CUSTOMER_VERIFICATION" -> CUSTOMER_VERIFICATION
            "SUCCEED" -> SUCCEED
            else -> throw IllegalArgumentException()
        }
    }
}