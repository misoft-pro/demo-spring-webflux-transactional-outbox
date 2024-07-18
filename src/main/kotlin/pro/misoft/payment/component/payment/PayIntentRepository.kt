package pro.misoft.payment.component.payment

import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class PayIntentRepository(private val template: R2dbcEntityTemplate) {

    private val log = LoggerFactory.getLogger(PayIntentRepository::class.java)

    suspend fun insert(payment: PaymentIntent): PaymentIntent {
        val payIntent = template.insert(PaymentIntent::class.java)
            .using(payment)
            .awaitSingle()
        log.info("New payment intent inserted for requestId=${payment.requestId}")
        return payIntent
    }

    suspend fun findById(id: Long): PaymentIntent? =
        template.select(PaymentIntent::class.java)
            .matching(
                Query.query(
                    Criteria.where("id").`is`(id)
                )
            )
            .awaitOneOrNull()

    suspend fun findByRequestId(requestId: String): PaymentIntent? =
        template.select(PaymentIntent::class.java)
            .matching(
                Query.query(
                    Criteria.where("requestId").`is`(requestId)
                )
            )
            .awaitOneOrNull()
}
