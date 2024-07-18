package pro.misoft.payment.web.api;

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pro.misoft.payment.common.DuplicatedEntityException
import pro.misoft.payment.component.payment.PayIntentRepository
import pro.misoft.payment.component.risk.RiskCheckService
import pro.misoft.payment.component.transaction.TxAndOutboxPair
import pro.misoft.payment.component.transaction.TxComponent
import pro.misoft.payment.component.transaction.TxRepository

@Component
class PayFacade(
    private val txComponent: TxComponent,
    private val riskCheckService: RiskCheckService,
    private val payRepository: PayIntentRepository,
    private val txRepository: TxRepository
) {

    private val log: Logger = LoggerFactory.getLogger(PayFacade::class.java)

    @Transactional
    suspend fun processPaymentIntent(paymentRequest: PaymentRequest): TxAndOutboxPair {
        log.info("Started processing pay request with requestId=${paymentRequest.requestId}")
        var intent = payRepository.findByRequestId(paymentRequest.requestId)
        if (intent != null) {
            log.info("Payment entity already exist for requestId=${paymentRequest.requestId}")
            throw DuplicatedEntityException("Payment request already exist for requestId=${paymentRequest.requestId}")
        }
        intent = payRepository.insert(paymentRequest.toEntity())
        val riskCheckStatus = riskCheckService.performRiskCheck(paymentRequest)

        val txOutboxPair =
            txComponent.processPayment(paymentRequest.toPaymentIntentCreatedEvent(intent.id, riskCheckStatus))
        txRepository.insert(txOutboxPair.txEntity)

        log.info("New tx is successfully placed for requestId=${intent.requestId}")
        return txOutboxPair
    }
}
