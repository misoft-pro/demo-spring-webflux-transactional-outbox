package pro.misoft.payment.component.transaction

import pro.misoft.payment.common.EntityNotFoundException
import pro.misoft.payment.component.risk.RiskCheckStatus
import kotlinx.coroutines.reactor.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.bind
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Repository
class TxRepository(private val template: R2dbcEntityTemplate, private val databaseClient: DatabaseClient) {

    private val log = LoggerFactory.getLogger(javaClass)

    suspend fun insert(entity: TxEntity): TxEntity {
        val tx = template.insert(TxEntity::class.java)
            .using(entity)
            .awaitSingle()
        log.info("New tx[${tx.id}] is inserted for requestId=${entity.requestId}")
        return tx
    }

    suspend fun update(txEntity: TxEntity): TxEntity {
        val query = """
            UPDATE transactions SET 
                request_id = :requestId,
                payment_intent_id = :paymentIntentId,
                acquirer_id = :acquirerId,
                acquirer_tx_id = :acquirerTxId,
                challenge_url = :challengeUrl,
                merchant_id = :merchantId,
                merchant_redirect_url = :merchantRedirectUrl,
                card_number = :cardNumber,
                card_expiry_date = :cardExpiryDate,
                card_token = :cardToken,
                order_id = :orderId,
                order_amount = :orderAmount,
                order_currency = :orderCurrency,
                risk_check_status = :riskCheckStatus,
                payment_status = :paymentStatus,
                original_request = :originalRequest,
                last_modified_date = :lastModifiedDate
            WHERE id = :id
            RETURNING *
        """.trimIndent()

        return databaseClient.sql(query)
            .bind("id", txEntity.id)
            .bind("paymentIntentId", txEntity.paymentIntentId)
            .bind("requestId", txEntity.requestId)
            .bind("acquirerId", txEntity.acquirerId)
            .bind("acquirerTxId", txEntity.acquirerTxId)
            .bind("challengeUrl", txEntity.challengeUrl)
            .bind("merchantId", txEntity.merchantId)
            .bind("merchantRedirectUrl", txEntity.merchantRedirectUrl)
            .bind("cardNumber", txEntity.cardNumber)
            .bind("cardExpiryDate", txEntity.cardExpiryDate)
            .bind("cardToken", txEntity.cardToken)
            .bind("orderId", txEntity.orderId)
            .bind("orderAmount", txEntity.orderAmount)
            .bind("orderCurrency", txEntity.orderCurrency)
            .bind("riskCheckStatus", txEntity.riskCheckStatus.name)
            .bind("paymentStatus", txEntity.transactionStatus.name)
            .bind("lastModifiedDate", txEntity.lastModifiedDate ?: LocalDateTime.now(ZoneId.of("UTC")))
            .map { row, _ ->
                TxEntity(
                    id = row.get("id", Long::class.java)!!,
                    requestId = row.get("request_id", String::class.java)!!,
                    paymentIntentId = row.get("payment_intent_id", Long::class.java)!!,
                    acquirerTxId = row.get("acquirer_tx_id", String::class.java)!!,
                    acquirerId = row.get("acquirer_id", String::class.java)!!,
                    challengeUrl = row.get("challenge_url", String::class.java)!!,
                    merchantId = row.get("merchant_id", String::class.java)!!,
                    merchantRedirectUrl = row.get("merchant_redirect_url", String::class.java)!!,
                    cardNumber = row.get("card_number", String::class.java)!!,
                    cardExpiryDate = row.get("card_expiry_date", LocalDate::class.java)!!,
                    cardToken = row.get("card_token", String::class.java)!!,
                    orderId = row.get("order_id", String::class.java)!!,
                    orderAmount = row.get("order_amount", BigDecimal::class.java)!!,
                    orderCurrency = row.get("order_currency", String::class.java)!!,
                    riskCheckStatus = RiskCheckStatus.of(row.get("risk_check_status", String::class.java)!!),
                    transactionStatus = TxStatus.of(row.get("transaction_status", String::class.java)!!),
                    createdDate = row.get("created_date", LocalDateTime::class.java),
                    lastModifiedDate = row.get("last_modified_date", LocalDateTime::class.java)
                )
            }
            .awaitOneOrNull()
            ?: throw EntityNotFoundException("TransactionEntity with id=${txEntity.id} not found")
    }

    suspend fun updateByAuthResult(id: Long, status: TxStatus, acquirerTxId: String, challengeUrl: String) {
        val rowsUpdated = template.update(TxEntity::class.java)
            .matching(
                Query.query(
                    Criteria.where("id").`is`(id)
                )
            )
            .apply(
                Update.update("transaction_status", status)
                    .set("acquirer_tx_id", acquirerTxId)
                    .set("challenge_url", challengeUrl)
            ).awaitSingle()
        if (rowsUpdated == 1L) {
            log.info("Tx[$id] is updated with status=$status, rows affected=${rowsUpdated}")
        } else {
            log.warn("Tx should be present with id=$id, rows affected=${rowsUpdated}")
        }
    }

    suspend fun findById(id: Long): TxEntity? =
        template.select(TxEntity::class.java)
            .matching(
                Query.query(
                    Criteria.where("id").`is`(id)
                )
            )
            .awaitOneOrNull()

    suspend fun findByPaymentIntentId(paymentIntentId: Long): TxEntity? =
        template.select(TxEntity::class.java)
            .matching(
                Query.query(
                    Criteria.where("paymentIntentId").`is`(paymentIntentId)
                )
            )
            .awaitOneOrNull()

    suspend fun findByRequestId(requestId: String): TxEntity? =
        template.select(TxEntity::class.java)
            .matching(
                Query.query(
                    Criteria.where("requestId").`is`(requestId)
                )
            )
            .awaitOneOrNull()
}
