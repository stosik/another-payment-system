package pl.stosik.billing.core.port.driven

import pl.stosik.billing.models.domain.Currency
import pl.stosik.billing.models.domain.Money

interface CurrencyProvider {
    fun convert(amount: Money, toCurrency: Currency): Money
}