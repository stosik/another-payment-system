package pl.stosik.paygrind.models.domain

@JvmInline
value class InvoiceId(private val value: Int) {
    fun asInt() = value
}

data class Invoice(
    val id: InvoiceId,
    val customerId: CustomerId,
    val amount: Money,
    val status: InvoiceStatus
) {
    fun isPending() = status == InvoiceStatus.PENDING
    fun pay() = this.copy(status = InvoiceStatus.PAID)

    fun recalculate(amount: Money) = this.copy(amount = amount)
}


