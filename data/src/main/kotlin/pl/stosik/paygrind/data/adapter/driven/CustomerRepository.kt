package pl.stosik.paygrind.data.adapter.driven

import pl.stosik.paygrind.data.JooqEngine
import pl.stosik.paygrind.data.port.driven.CustomerFinder
import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.Customer
import pl.stosik.paygrind.models.domain.CustomerId

class CustomerRepository(private val db: JooqEngine) : CustomerFinder {

    override fun fetch(id: CustomerId): Customer? {
        return db.query {
            CustomerTable
                .select { CustomerTable.id eq id.asInt() }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    override fun fetchAll(): List<Customer> {
        return db.query {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(currency: Currency): Customer? {
        return db.query {
            CustomerTable.insert {
                it[CustomerTable.currency] = currency.toString()
            }.singleOrNull()?.toCustomer()
        }
    }
}

private fun ResultRow.toCustomer(): Customer = Customer(
    id = CustomerId(this[CustomerTable.id]),
    currency = Currency.valueOf(this[CustomerTable.currency])
)
