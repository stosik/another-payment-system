package pl.stosik.paygrind.core.infrastracture.adapter.driven

import pl.stosik.paygrind.core.port.driven.CurrencyProvider
import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.Money
import java.math.BigDecimal

/**
This is the mocked instance of the currency provider
which just returns the same amount in requested currency
 **/
class FixedCurrencyProvider : CurrencyProvider {
    override fun convert(amount: Money, toCurrency: Currency): Money {
        return when (amount.currency) {
            Currency.EUR -> Money(amount.value.multiply(EUR_FIXED_RATE), toCurrency)
            Currency.USD -> Money(amount.value.multiply(USD_FIXED_RATE), toCurrency)
            Currency.DKK -> Money(amount.value.multiply(DKK_FIXED_RATE), toCurrency)
            Currency.SEK -> Money(amount.value.multiply(SEK_FIXED_RATE), toCurrency)
            Currency.GBP -> Money(amount.value.multiply(GBP_FIXED_RATE), toCurrency)
        }
    }

    companion object {
        private val EUR_FIXED_RATE = BigDecimal.valueOf(1.5)
        private val USD_FIXED_RATE = BigDecimal.valueOf(2.0)
        private val DKK_FIXED_RATE = BigDecimal.valueOf(3.5)
        private val SEK_FIXED_RATE = BigDecimal.valueOf(3.0)
        private val GBP_FIXED_RATE = BigDecimal.valueOf(4.0)
    }
}