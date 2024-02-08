package pl.stosik.billing.core.infrastracture.adapter.driven

import pl.stosik.billing.core.port.driven.PaymentProvider
import pl.stosik.billing.models.domain.Invoice
import kotlin.random.Random

class RandomPaymentProvider : PaymentProvider {
    override fun charge(invoice: Invoice): Boolean {
        return Random.nextBoolean()
    }
}