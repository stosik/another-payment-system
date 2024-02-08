package pl.stosik.billing.models.domain

@JvmInline
value class CustomerId(private val value: Int) {
    fun asInt() = value
}

data class Customer(
    val id: CustomerId,
    val currency: Currency
)
