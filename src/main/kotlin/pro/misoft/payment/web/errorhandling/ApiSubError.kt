package pro.misoft.payment.web.errorhandling

data class ApiSubError(
    val objectName: String,
    val fieldName: String,
    val rejectedValue: Any?,
    val message: String
)

