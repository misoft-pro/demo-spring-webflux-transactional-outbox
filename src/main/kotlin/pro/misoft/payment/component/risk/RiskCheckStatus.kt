package pro.misoft.payment.component.risk

enum class RiskCheckStatus(val label: String) {
    NEW("NEW"),
    GREEN("GREEN"),
    YELLOW("YELLOW"),
    RED("RED");

    companion object {
        public fun of(label: String): RiskCheckStatus = when (label) {
            "NEW" -> NEW
            "GREEN" -> GREEN
            "YELLOW" -> YELLOW
            "RED" -> RED
            else -> throw IllegalArgumentException("Unknown label: $label")
        }
    }
}
