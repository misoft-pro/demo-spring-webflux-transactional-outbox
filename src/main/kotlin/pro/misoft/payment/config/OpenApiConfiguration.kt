package pro.misoft.payment.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "Payment Gateway API",
        version = "v1",
        description = "Payment Gateway API"
    )
)
@Configuration
class OpenApiDocConfiguration {
}