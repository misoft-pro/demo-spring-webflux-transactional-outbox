package pro.misoft.payment.web.api

import pro.misoft.payment.TestConfigApiServerApplication
import pro.misoft.payment.component.payment.PayIntentRepository
import pro.misoft.payment.component.risk.RiskCheckService
import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.component.transaction.TxRepository
import pro.misoft.payment.component.transaction.TxStatus
import pro.misoft.payment.outbox.OutboxEventRepository
import com.ninjasquad.springmockk.SpykBean
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestConfigApiServerApplication::class)
class PaymentApiComponentTest {

    @SpykBean
    private lateinit var riskCheckService: RiskCheckService

    @Autowired
    private lateinit var payRepository: PayIntentRepository

    @Autowired
    private lateinit var txRepository: TxRepository

    @Autowired
    private lateinit var payApi: PaymentApi

    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Test
    fun `should persist and process tx successfully when risk check passed`() {
        runBlocking {
            val payRequest = PaymentRequestMother.newPaymentRequest()
            coEvery { riskCheckService.performRiskCheck(any()) } returns RiskCheckStatus.GREEN

            val resp: PaymentResponse = runBlocking { payApi.processPayment(payRequest) }

            assertThat(resp.txId).isNotNull()
            assertThat(resp.challengeUrl).isNotNull()
            assertThat(payRepository.findByRequestId(payRequest.requestId)).isNotNull
            txRepository.findById(resp.txId.toLong()).run {
                assertThat(this).isNotNull
                assertThat(this!!.transactionStatus).isEqualTo(TxStatus.CUSTOMER_VERIFICATION)
                assertThat(this.challengeUrl).isNotNull()
                assertThat(this.riskCheckStatus).isEqualTo(RiskCheckStatus.GREEN)
            }
            assertThat(outboxEventRepository.findById(resp.txId.toLong())).isNull()
        }
    }

    @Test
    fun `should persist but not process tx when risk check is not passed`() {
        runBlocking {
            val payRequest = PaymentRequestMother.newPaymentRequest()
            coEvery { riskCheckService.performRiskCheck(any()) } returns RiskCheckStatus.YELLOW

            val resp: PaymentResponse = runBlocking { payApi.processPayment(payRequest) }

            assertThat(resp.txId).isNotNull()
            assertThat(payRepository.findByRequestId(payRequest.requestId)).isNotNull
            txRepository.findById(resp.txId.toLong()).run {
                assertThat(this).isNotNull
                assertThat(this!!.transactionStatus).isEqualTo(TxStatus.NEW)
                assertThat(this.challengeUrl).isNull()
            }
        }
    }

    @Test
    fun `should rollback payment when risk check fails`() {
        runBlocking {
            val payRequest = PaymentRequestMother.newPaymentRequest()

            val errorMessage = "error msg"
            coEvery { riskCheckService.performRiskCheck(any()) } throws RuntimeException(errorMessage)

            assertThatThrownBy { runBlocking { payApi.processPayment(payRequest) } }
                .isInstanceOf(
                    RuntimeException::class.java
                )
                .hasMessage(errorMessage)

            assertThat(payRepository.findByRequestId(payRequest.requestId)).isNull()
            assertThat(txRepository.findByRequestId(payRequest.requestId)).isNull()
        }
    }
}