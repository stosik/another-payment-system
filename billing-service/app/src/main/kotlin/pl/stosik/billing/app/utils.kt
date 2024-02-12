package pl.stosik.billing.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import pl.stosik.billing.data.adapter.driven.CustomerRepository
import pl.stosik.billing.data.adapter.driven.InvoiceRepository
import pl.stosik.billing.models.domain.Currency
import pl.stosik.billing.models.domain.InvoiceStatus
import pl.stosik.billing.models.domain.Money
import pl.stosik.billing.models.infrastracture.ApplicationConfiguration
import pl.stosik.billing.models.infrastracture.ApplicationConfiguration.KafkaConfiguration.BootstrapServers
import java.io.InputStream
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random


private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

fun parseConfiguration(file: InputStream?): ApplicationConfiguration {
    requireNotNull(file) {
        "Configuration file not found"
    }
    return file
        .use { mapper.readValue(it, ApplicationConfiguration::class.java) }
        .let {
            it.copy(
                cron = it.cron.copy(
                    instanceId = System.getenv("INSTANCE_ID") ?: UUID.randomUUID().toString(),
                    expression = System.getenv("CRON_EXPRESSION") ?: it.cron.expression
                ),
                database = it.database.copy(
                    url = System.getenv("DATABASE_URL") ?: it.database.url,
                    username = System.getenv("DATABASE_USERNAME") ?: it.database.username,
                    password = System.getenv("DATABASE_PASSWORD") ?: it.database.password
                ),
                kafka = it.kafka.copy(
                    bootstrapServers = BootstrapServers(
                        System.getenv("KAFKA_URL")?.split(",") ?: it.kafka.bootstrapServers.servers
                    )
                )
            )
        }
}

fun setupInitialData(customerRepository: CustomerRepository, invoiceRepository: InvoiceRepository) {
    val customers = (1..100).mapNotNull {
        customerRepository.createCustomer(
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)]
        )
    }

    customers.forEach { customer ->
        (1..10).forEach {
            invoiceRepository.createInvoice(
                amount = Money(
                    value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                    currency = customer.currency
                ),
                customer = customer,
                status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
            )
        }
    }
}