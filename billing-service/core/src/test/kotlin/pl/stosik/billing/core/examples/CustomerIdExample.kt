package pl.stosik.billing.core.examples

import pl.stosik.billing.models.domain.CustomerId
import kotlin.random.Random

object CustomerIdExample {

    fun random() = CustomerId(Random.nextInt(0, 999))
}