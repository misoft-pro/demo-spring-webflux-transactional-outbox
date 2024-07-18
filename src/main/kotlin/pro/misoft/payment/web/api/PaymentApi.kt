package pro.misoft.payment.web.api

import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.component.transaction.backflow.TxAuthResultPoller
import pro.misoft.payment.outbox.OutboxEventProcessor
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pro.misoft.payment.web.validation.ConstraintValidator

@RestController
@RequestMapping("/v1/payments")
@Tag(name = "Payments", description = "The Payment API.")
class PaymentApi(
    private val txResultPoller: TxAuthResultPoller,
    private val payFacade: PayFacade,
    private val outboxEventProcessor: OutboxEventProcessor,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,

    ) {
    private val log: Logger = LoggerFactory.getLogger(PaymentApi::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    suspend fun processPayment(@RequestBody paymentRequest: PaymentRequest): PaymentResponse {
        log.info("About to place payment request: $paymentRequest")
        ConstraintValidator.validate(paymentRequest)

        val (txEntity, outboxMessage) = payFacade.processPaymentIntent(paymentRequest)

        outboxMessage?.let { CoroutineScope(dispatcher).launch { outboxEventProcessor.processOutboxEvent(it) } }

        val paymentResponse =
            PaymentResponse.of(txEntity.id.toString(), txEntity.transactionStatus, txEntity.riskCheckStatus)

        if (txEntity.riskCheckStatus == RiskCheckStatus.GREEN) {
            val payAuthResult = txResultPoller.pollTxAuthResult(txEntity.id)
            paymentResponse.update(payAuthResult)
        }

        log.info("Payment request is completed with txId=${txEntity.id}")
        return paymentResponse
    }
}