package pl.stosik.billing.data.port.driven

import pl.stosik.billing.models.domain.Customer
import pl.stosik.billing.models.domain.CustomerId

interface CustomerFinder {
    fun fetchAll(): List<Customer>
    fun fetch(id: CustomerId): Customer?
}