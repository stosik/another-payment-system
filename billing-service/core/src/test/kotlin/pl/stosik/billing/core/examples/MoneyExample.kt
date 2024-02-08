package pl.stosik.billing.core.examples

import pl.stosik.billing.models.domain.Currency
import pl.stosik.billing.models.domain.Money
import kotlin.random.Random

object MoneyExample {

    fun random() = Money(
        value = Random.nextInt(0, 999).toBigDecimal(),
        currency = Currency.values().toList().shuffled().first()
    )
}