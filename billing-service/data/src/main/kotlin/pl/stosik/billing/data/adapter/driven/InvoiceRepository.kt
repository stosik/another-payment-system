package pl.stosik.billing.data.adapter.driven

import org.jetbrains.exposed.sql.*
import pl.stosik.billing.data.asyncQuery
import pl.stosik.billing.data.port.driven.InvoiceFinder
import pl.stosik.billing.data.port.driven.InvoiceUpdater
import pl.stosik.billing.data.query
import pl.stosik.billing.data.singleOrNull
import pl.stosik.billing.models.domain.*

object InvoiceTable : Table() {
    val id = integer("id").autoIncrement()
    val currency = varchar("currency", 3)
    val value = decimal("value", 1000, 2)
    val customerId = reference("customer_id", CustomerTable.id)
    val status = text("status").index("idx_invoice_status")
    override val primaryKey = PrimaryKey(columns = arrayOf(id), name = "pk_invoice_id")
}

class InvoiceRepository(private val db: Database) : InvoiceFinder, InvoiceUpdater {

    override fun fetch(id: InvoiceId): Invoice? {
        return db.query {
            InvoiceTable
                .select { InvoiceTable.id eq id.asInt() }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    override fun fetchAll(): List<Invoice> {
        return db.query {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    override suspend fun fetchAllPending(limit: Int, offset: Long): List<InvoiceId> {
        return db.asyncQuery {
            InvoiceTable
                .slice(InvoiceTable.id)
                .select { InvoiceTable.status eq InvoiceStatus.PENDING.name }
                .limit(n = limit, offset = offset)
                .map { InvoiceId(it[InvoiceTable.id]) }
        }
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus): Invoice? {
        return db.query {
            InvoiceTable
                .insert {
                    it[value] = amount.value
                    it[currency] = amount.currency.toString()
                    it[InvoiceTable.status] = status.toString()
                    it[customerId] = customer.id.asInt()
                }
                .singleOrNull()
                ?.toInvoice()
        }
    }

    override suspend fun updateStatus(id: InvoiceId, status: InvoiceStatus): Invoice? {
        db.asyncQuery {
            InvoiceTable
                .update({ InvoiceTable.id eq id.asInt() }) {
                    it[InvoiceTable.status] = status.toString()
                }
        }

        return fetchInvoiceAsync(id)
    }

    private suspend fun fetchInvoiceAsync(id: InvoiceId): Invoice? {
        return db.asyncQuery {
            InvoiceTable
                .select { InvoiceTable.id eq id.asInt() }
                .firstOrNull()
                ?.toInvoice()
        }
    }
}

private fun ResultRow.toInvoice(): Invoice = Invoice(
    id = InvoiceId(this[InvoiceTable.id]),
    amount = Money(
        value = this[InvoiceTable.value],
        currency = Currency.valueOf(this[InvoiceTable.currency])
    ),
    status = InvoiceStatus.valueOf(this[InvoiceTable.status]),
    customerId = CustomerId(this[InvoiceTable.customerId])
)