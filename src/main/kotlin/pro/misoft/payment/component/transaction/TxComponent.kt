package pro.misoft.payment.component.transaction

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pro.misoft.payment.component.payment.PaymentIntentCreatedEvent
import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.outbox.OutboxEvent
import pro.misoft.payment.outbox.Publisher
import java.math.BigDecimal

@Component
class TxComponent(private val publisher: Publisher) {

    private val log: Logger = LoggerFactory.getLogger(TxComponent::class.java)

    suspend fun processPayment(event: PaymentIntentCreatedEvent): TxAndOutboxPair {
        log.info("Started processing of ${PaymentIntentCreatedEvent::class.simpleName} for requestId=${event.requestId} and intentId=${event.intentId}")

        val txEntity = applyBusinessLogic(TxEntity(event))

        val txAndOutboxPair = TxAndOutboxPair(txEntity)
        if (event.riskCheckStatus == RiskCheckStatus.GREEN) {
            log.info("Risk check is passed and tx processing started for txId=${txEntity.id}")
            val outboxEvent = OutboxEvent(
                id = txEntity.id,
                classType = TxEntity::class.java.name,
                serContent = "//TODO serialize txEntity to tx created event"
            )
            publisher.publish(outboxEvent)

            txAndOutboxPair.outboxEvent = outboxEvent
        } else {
            log.warn("Risk check is not passed for txId=${txEntity.id}")
        }
        return txAndOutboxPair
    }

    private fun applyBusinessLogic(entity: TxEntity): TxEntity {
        //TODO implement business logic
        return entity
    }
}