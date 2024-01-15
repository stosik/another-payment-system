package pl.stosik.paygrind.models.domain

@JvmInline
value class CustomerId(private val value: Int) {
    fun asInt() = value
}

data class Customer(
    val id: CustomerId,
    val currency: Currency
)
