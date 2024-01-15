package pl.stosik.paygrind.data.port.driven

import pl.stosik.paygrind.models.domain.Customer
import pl.stosik.paygrind.models.domain.CustomerId

interface CustomerFinder {
    fun fetchAll(): List<Customer>
    fun fetch(id: CustomerId): Customer?
}