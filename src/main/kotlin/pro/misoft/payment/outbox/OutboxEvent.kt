package pro.misoft.payment.outbox

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.ZoneId

@Table("outbox_events")
data class OutboxEvent(
    @Id val id: Long,
    val classType: String,
    val serContent: String,
    val createdAt: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
)