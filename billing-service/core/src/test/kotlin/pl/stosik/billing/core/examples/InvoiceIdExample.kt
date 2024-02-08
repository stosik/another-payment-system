package pl.stosik.billing.core.examples

import pl.stosik.billing.models.domain.InvoiceId
import kotlin.random.Random

object InvoiceIdExample {

    fun random() = InvoiceId(Random.nextInt(0, 999))
}