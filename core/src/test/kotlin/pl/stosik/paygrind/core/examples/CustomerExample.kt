package pl.stosik.paygrind.core.examples

import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.Customer

object CustomerExample {

    fun random() = Customer(
        id = CustomerIdExample.random(),
        currency = Currency.values().toList().shuffled().first()
    )
}