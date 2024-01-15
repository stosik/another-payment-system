package pl.stosik.paygrind.core.examples

import pl.stosik.paygrind.models.domain.CustomerId
import kotlin.random.Random

object CustomerIdExample {

    fun random() = CustomerId(Random.nextInt(0, 999))
}