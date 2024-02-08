package pl.stosik.billing.app

import arrow.fx.coroutines.ResourceScope
import pl.stosik.billing.core.infrastracture.adapter.driven.*
import pl.stosik.billing.core.infrastracture.adapter.driver.ChargeInvoiceEventSink
import pl.stosik.billing.core.port.driven.TimeProvider
import pl.stosik.billing.core.services.billing.BillingProcessor
import pl.stosik.billing.core.services.billing.BillingService
import pl.stosik.billing.core.services.customer.CustomerService
import pl.stosik.billing.core.services.invoice.InvoiceService
import pl.stosik.billing.core.services.job_lock.JobLockService
import pl.stosik.billing.data.adapter.driven.CustomerRepository
import pl.stosik.billing.data.adapter.driven.InvoiceRepository
import pl.stosik.billing.data.adapter.driven.JobLockRepository
import pl.stosik.billing.data.exposed
import pl.stosik.billing.models.infrastracture.ApplicationConfiguration
import pl.stosik.billing.models.infrastracture.joinKeysFlattening
import pl.stosik.messaging.kafka.KafkaConfiguration

class Dependencies(
    val jobLockService: JobLockService,
    val invoiceService: InvoiceService,
    val customerService: CustomerService,
    val billingService: BillingService,
    val billingProcessor: BillingProcessor,
    val timeProvider: TimeProvider
)

suspend fun ResourceScope.dependencies(configuration: ApplicationConfiguration): Dependencies {
    val exposedEngine = exposed(configuration = configuration.database)

    // Data access layer
    val customerRepository = CustomerRepository(db = exposedEngine)
    val invoiceRepository = InvoiceRepository(db = exposedEngine)
    val jobLockRepository = JobLockRepository(db = exposedEngine)

    // External system dependencies
    val paymentProvider = RandomPaymentProvider()
    val currencyProvider = FixedCurrencyProvider()

    // Domain services
    val jobLockService = JobLockService(jobLockAcquirer = jobLockRepository)
    val invoiceService = InvoiceService(invoiceFinder = invoiceRepository, invoiceUpdater = invoiceRepository)
    val customerService = CustomerService(customerFinder = customerRepository)
    val billingService = BillingService(
        customerService = customerService,
        invoiceService = invoiceService,
        paymentProvider = paymentProvider,
        currencyProvider = currencyProvider,
        notifiers = listOf(EmailNotifier(), TelemetryNotifier())
    )

    val kafka = KafkaConfiguration.configure(
        consumerConfig = configuration.kafka.consumer.toMap().joinKeysFlattening(),
        producerConfig = configuration.kafka.producer.toMap().joinKeysFlattening(),
        sinks = listOf(
            ChargeInvoiceEventSink(
                billingService = billingService
            )
        )
    )

    val chargeInvoiceSource = ChargeInvoiceSource(
        producer = kafka.createProducer(
            topic = ChargeInvoiceSource.TOPIC
        )
    )

    val billingProcessor = BillingProcessor(
        chargeInvoiceSource = chargeInvoiceSource,
        invoiceService = invoiceService
    )

    setupInitialData(customerRepository, invoiceRepository)

    return Dependencies(
        jobLockService = jobLockService,
        invoiceService = invoiceService,
        customerService = customerService,
        billingService = billingService,
        billingProcessor = billingProcessor,
        timeProvider = CurrentTimeProvider()
    )
}