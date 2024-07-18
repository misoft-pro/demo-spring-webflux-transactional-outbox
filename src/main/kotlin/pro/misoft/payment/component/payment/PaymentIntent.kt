package pro.misoft.payment.component.payment

import pro.misoft.payment.common.IdGenerator
import io.r2dbc.postgresql.codec.Json
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.ZoneId

@Table("intents")
data class PaymentIntent(
    @Id val id: Long = IdGenerator.uniqueId(),
    val requestId: String,
    val originalRequest: Json,
    @CreatedDate var createdDate: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC")),
)