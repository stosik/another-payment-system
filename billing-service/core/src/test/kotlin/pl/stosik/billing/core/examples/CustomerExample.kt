package pl.stosik.billing.core.examples

import pl.stosik.billing.models.domain.Currency
import pl.stosik.billing.models.domain.Customer

object CustomerExample {

    fun random() = Customer(
        id = CustomerIdExample.random(),
        currency = Currency.values().toList().shuffled().first()
    )
}