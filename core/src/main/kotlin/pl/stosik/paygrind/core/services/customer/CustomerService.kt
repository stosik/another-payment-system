package pl.stosik.paygrind.core.services.customer

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import pl.stosik.paygrind.data.port.driven.CustomerFinder
import pl.stosik.paygrind.models.domain.Customer
import pl.stosik.paygrind.models.domain.CustomerId
import pl.stosik.paygrind.models.domain.errors.PaygrindError.BillingError.NonRetryableError.CustomerNotFound

class CustomerService(private val customerFinder: CustomerFinder) {
    fun fetchAll(): List<Customer> {
        return customerFinder.fetchAll()
    }

    fun fetch(id: CustomerId): Either<CustomerNotFound, Customer> = either {
        val customer = customerFinder.fetch(id)
        ensureNotNull(customer) {
            CustomerNotFound(id)
        }
    }
}
