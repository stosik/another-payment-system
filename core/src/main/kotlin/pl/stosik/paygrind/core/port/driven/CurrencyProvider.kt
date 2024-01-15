package pl.stosik.paygrind.core.port.driven

import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.Money

interface CurrencyProvider {
    fun convert(amount: Money, toCurrency: Currency): Money
}