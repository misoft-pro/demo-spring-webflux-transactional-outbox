package pro.misoft.payment.component.transaction

import pro.misoft.payment.component.payment.PaymentIntentCreatedEventMother
import pro.misoft.payment.component.transaction.TxEntity

object TxEntityMother {
    fun newTxEntity(): TxEntity {
        return TxEntity(PaymentIntentCreatedEventMother.newPaymentIntentCreatedEvent())
    }
}