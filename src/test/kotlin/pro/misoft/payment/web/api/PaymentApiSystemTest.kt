package pro.misoft.payment.web.api

import pro.misoft.payment.TestConfigApiServerApplication
import pro.misoft.payment.common.DownstreamTimeoutException
import pro.misoft.payment.common.IdGenerator
import pro.misoft.payment.component.risk.RiskCheckStatus
import pro.misoft.payment.component.transaction.TxStatus
import pro.misoft.payment.component.transaction.backflow.TxAuthResultPoller
import com.ninjasquad.springmockk.SpykBean
import io.mockk.coEvery
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestConfigApiServerApplication::class)
class PaymentApiSystemTest : AbstractSystemTest() {

    @SpykBean
    private lateinit var txAuthResultPoller: TxAuthResultPoller

    fun paymentRequestJson(requestId: Long, merchantId: String = "12345678") = """
        {
          "requestId" : "$requestId",
          "merchantRedirectUrl": "https://merchant.com/redirect",
            "merchantId": "$merchantId",
          "card": {
            "number": "4111******111111",
            "expiryDate": "12/25",
            "cardToken": "abcdef123456"
          },
          "order": {
            "id": "order-1234",
            "total": {
              "amount": "100.00",
              "currency": "USD"
            }
          }
        }
        """.trimIndent()

    @Test
    fun `should return 200 with challengeUrl when Post payment to the API`() {
        Given {
            contentType(ContentType.JSON)
            body(paymentRequestJson(IdGenerator.uniqueId()))
        } When {
            post("/v1/payments")
        } Then {
            statusCode(200)
            body("txId", Matchers.notNullValue())
            body("challengeUrl", Matchers.notNullValue())
            body("transactionStatus", Matchers.equalTo(TxStatus.CUSTOMER_VERIFICATION.label))
            body("riskCheckStatus", Matchers.equalTo(RiskCheckStatus.GREEN.label))
        }
    }

    @Test
    fun `should return 400 when Post bad request with blank merchantId to the API`() {
        Given {
            contentType(ContentType.JSON)
            body(paymentRequestJson(IdGenerator.uniqueId(), ""))
        } When {
            post("/v1/payments")
        } Then {
            statusCode(400)
            body("httpStatus", Matchers.equalTo(400))
            body("errorMessage", Matchers.equalTo("Input fields contain errors"))
            body("subErrors[0].fieldName", Matchers.equalTo("merchantId"))
            body("subErrors[0].message", Matchers.equalTo("must not be blank"))
            body("internalCode", Matchers.notNullValue())
            body("traceId", Matchers.notNullValue())
        }
    }

    @Test
    fun `should return 409 when duplicate Post payment to the API`() {
        Given {
            contentType(ContentType.JSON)
            body(paymentRequestJson(IdGenerator.uniqueId()))
        } When {
            post("/v1/payments")
            post("/v1/payments")
        } Then {
            statusCode(409)
            body("errorMessage", Matchers.equalTo("Entity already exist"))
        }
    }

    @Test
    fun `should return 504 timeout when tx is placed but timeout happens when waiting for auth result`() {
        coEvery { txAuthResultPoller.pollTxAuthResult(any()) } throws DownstreamTimeoutException("Payment authentication timed out")

        Given {
            contentType(ContentType.JSON)
            body(paymentRequestJson(IdGenerator.uniqueId()))
        } When {
            post("/v1/payments")
        } Then {
            statusCode(504)
            body("httpStatus", Matchers.equalTo(504))
            body(
                "errorMessage",
                Matchers.equalTo("Some external service does not respond within the expected timeframe")
            )
            body("internalCode", Matchers.notNullValue())
            body("traceId", Matchers.notNullValue())
        }
    }
}