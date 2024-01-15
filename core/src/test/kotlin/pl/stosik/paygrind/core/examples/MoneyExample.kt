package pl.stosik.paygrind.core.examples

import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.Money
import kotlin.random.Random

object MoneyExample {

    fun random() = Money(
        value = Random.nextInt(0, 999).toBigDecimal(),
        currency = Currency.values().toList().shuffled().first()
    )
}