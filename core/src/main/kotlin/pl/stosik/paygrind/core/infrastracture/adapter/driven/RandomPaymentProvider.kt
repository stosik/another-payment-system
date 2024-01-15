package pl.stosik.paygrind.core.infrastracture.adapter.driven

import pl.stosik.paygrind.core.port.driven.PaymentProvider
import pl.stosik.paygrind.models.domain.Invoice
import kotlin.random.Random

class RandomPaymentProvider : PaymentProvider {
    override fun charge(invoice: Invoice): Boolean {
        return Random.nextBoolean()
    }
}