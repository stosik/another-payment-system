package pl.stosik.paygrind.models.domain

import java.math.BigDecimal

data class Money(
    val value: BigDecimal,
    val currency: Currency
)
