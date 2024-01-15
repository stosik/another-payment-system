package pl.stosik.paygrind.core.examples

import pl.stosik.paygrind.models.domain.InvoiceId
import kotlin.random.Random

object InvoiceIdExample {

    fun random() = InvoiceId(Random.nextInt(0, 999))
}