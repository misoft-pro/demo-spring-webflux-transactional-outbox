package pro.misoft.payment.web.errorhandling


import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.slf4j.MDC
import org.springframework.context.support.AbstractResourceBasedMessageSource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import java.util.*

@Component
class ApiErrorFactory(private val msgSource: AbstractResourceBasedMessageSource) {

    fun error(ex: Exception): ApiError {
        val errorKeys: ApiErrorKeys = RestErrorMapping.getKeys(ex)

        val message = msgSource.getMessage(errorKeys.i18nKey, emptyArray(), ex.message, Locale.ENGLISH)
        return ApiError(
            errorKeys.httpStatus,
            errorKeys.internalCode,
            message ?: error("Error message is not specified")
        )
    }

    fun error(ex: MethodArgumentNotValidException): ApiError {
        return error(ex.bindingResult.fieldErrors)
    }

    private fun error(fieldErrors: List<FieldError>): ApiError {
        return ApiError(
            HttpStatus.BAD_REQUEST.value(),
            "" + 4002,
            "Input fields contain errors",
            MDC.get("traceId"),
            fieldErrors.map { fe: FieldError ->
                ApiSubError(
                    fe.objectName,
                    fe.field,
                    fe.rejectedValue,
                    fe.defaultMessage ?: ""
                )
            }
        )
    }

    fun error(ex: ConstraintViolationException): ApiError {
        val result = HashMap<String, String>()
        for (cv in ex.constraintViolations) {
            if (result.containsKey(cv.propertyPath.toString())) {
                if (contains(cv, NotEmpty::class.java) || contains(
                        cv,
                        NotNull::class.java
                    ) || contains(
                        cv,
                        NotBlank::class.java
                    )
                ) {
                    result[cv.propertyPath.toString()] = cv.messageTemplate
                }
            } else {
                result[cv.propertyPath.toString()] = cv.messageTemplate
            }
        }
        val errors: List<FieldError> = ex.constraintViolations
            .stream()
            .filter { cv ->
                result.containsKey(cv.propertyPath.toString()) && result[cv.propertyPath.toString()]!!.contains(cv.messageTemplate)
            }
            .map { cv ->
                FieldError(
                    cv.rootBeanClass.getSimpleName(),
                    cv.propertyPath.toString(),
                    if (cv.invalidValue != null) cv.invalidValue.toString() else "",
                    true,
                    null,
                    null,
                    getCvMsg(
                        cv.rootBeanClass.getSimpleName().lowercase(Locale.getDefault()) + "." + cv.propertyPath.toString()
                            .lowercase(Locale.getDefault()), cv.message
                    )
                )
            }
            .toList()
        return error(errors)
    }

    private fun getCvMsg(key: String, defaultMessage: String): String? {
        return msgSource.getMessage("errors.$key", null, defaultMessage, Locale.ENGLISH)
    }


    private fun contains(cv: ConstraintViolation<*>, claz: Class<*>): Boolean {
        return cv.messageTemplate.contains(claz.simpleName.lowercase(Locale.getDefault()))
    }
}