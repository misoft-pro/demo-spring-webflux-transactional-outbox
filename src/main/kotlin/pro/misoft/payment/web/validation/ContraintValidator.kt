package pro.misoft.payment.web.validation


import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.executable.ExecutableValidator
import java.lang.reflect.Method


object ConstraintValidator {
    private val validator: Validator = Validation.buildDefaultValidatorFactory().validator
    private val execValidator: ExecutableValidator =
        Validation.buildDefaultValidatorFactory().validator.forExecutables()

    /**
     * Validates an object using default validator which doesn't include localised error message interpolation
     * from `message` annotation parameter like `@NotEmpty(message = "{errors.token.password.notempty}")`
     * @param `object`
     * @param <T>
    </T> */
    fun <T> validate(obj: T) {
        validate(obj, validator)
    }

    fun <T> validate(obj: T, validator: Validator) {
        val violations: Set<ConstraintViolation<T>> = validator.validate(obj)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }


    fun <T> validateMethod(obj: T, methodName: String, parameterValues: Array<String>) {
        val method: Method
        try {
            val classes = parameterValues.map { String::class.java }
            method = obj!!::class.java.getMethod(methodName, *classes.toTypedArray())
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException(e)
        }

        val violations: Set<ConstraintViolation<T>> = execValidator.validateParameters(obj, method, parameterValues)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }
}
