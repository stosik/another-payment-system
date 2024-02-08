package pl.stosik.billing.e2e

import arrow.fx.coroutines.continuations.ResourceScope
import pl.stosik.billing.core.infrastracture.adapter.driven.*
import pl.stosik.billing.core.infrastracture.adapter.driver.BillingJobScheduler
import pl.stosik.billing.core.infrastracture.adapter.driver.ChargeInvoiceEventSink
import pl.stosik.billing.core.infrastracture.adapter.driver.billingJobScheduler
import pl.stosik.billing.core.services.billing.BillingProcessor
import pl.stosik.billing.core.services.billing.BillingService
import pl.stosik.billing.core.services.customer.CustomerService
import pl.stosik.billing.core.services.invoice.InvoiceService
import pl.stosik.billing.core.services.job_lock.JobLockService
import pl.stosik.billing.data.adapter.driven.InvoiceRepository
import pl.stosik.billing.data.adapter.driven.JobLockRepository
import pl.stosik.billing.data.exposed
import pl.stosik.billing.models.infrastracture.ApplicationConfiguration.CronConfiguration
import pl.stosik.billing.models.infrastracture.ApplicationConfiguration.KafkaConfiguration.BootstrapServers
import pl.stosik.billing.models.infrastracture.joinKeysFlattening
import pl.stosik.billing.app.parseConfiguration
import pl.stosik.billing.data.adapter.driven.CustomerRepository
import pl.stosik.messaging.kafka.KafkaConfiguration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class TestDependencies(
    val invoiceRepository: InvoiceRepository,
    val customerRepository: CustomerRepository,
    val invoiceService: InvoiceService,
    val billingJobScheduler: BillingJobScheduler,
)

suspend fun ResourceScope.testDependencies(): TestDependencies {

    val containers = AppContainers.apply { start() }
    val currentTimeProvider = CurrentTimeProvider()

    val applicationConfiguration =
        parseConfiguration(this::class.java.classLoader.getResourceAsStream("application-test.yaml"))
            .let {
                it.copy(
                    cron = CronConfiguration(
                        instanceId = "test-instance-id",
                        expression = getCronExpressionForImmediateExecution(currentTimeProvider.now())
                    ),
                    database = it.database.copy(
                        url = AppContainers.jdbcUrl,
                        username = AppContainers.dbUsername,
                        password = AppContainers.dbPassword
                    ),
                    kafka = it.kafka.copy(
                        consumer = it.kafka.consumer.copy(
                            bootstrap = BootstrapServers(AppContainers.bootstrapServers)
                        ),
                        producer = it.kafka.producer.copy(
                            bootstrap = BootstrapServers(AppContainers.bootstrapServers)
                        )
                    )
                )
            }

    val exposedEngine = exposed(applicationConfiguration.database)

    val customerRepository = CustomerRepository(db = exposedEngine)
    val invoiceRepository = InvoiceRepository(db = exposedEngine)
    val jobLockRepository = JobLockRepository(db = exposedEngine)

    val currencyProvider = FixedCurrencyProvider()
    val paymentProvider = RandomPaymentProvider()
    val timeProvider = CurrentTimeProvider()

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
        consumerConfig = applicationConfiguration.kafka.consumer.toMap().joinKeysFlattening(),
        producerConfig = applicationConfiguration.kafka.producer.toMap().joinKeysFlattening(),
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

    val billingJobScheduler = billingJobScheduler(
        cronConfiguration = applicationConfiguration.cron,
        billingProcessor = billingProcessor,
        jobLockService = jobLockService,
        timeProvider = timeProvider
    )

    return TestDependencies(
        invoiceRepository = invoiceRepository,
        customerRepository = customerRepository,
        invoiceService = invoiceService,
        billingJobScheduler = billingJobScheduler
    )
}

private fun getCronExpressionForImmediateExecution(currentTime: LocalDateTime): String {
    val cronExpressionAfterAppStart = DateTimeFormatter
        .ofPattern("s m H")
        .withZone(ZoneId.systemDefault())
        .format(
            currentTime
                .plusSeconds(10)
                .toInstant(ZoneOffset.UTC)
        )

    return "$cronExpressionAfterAppStart * * ?"
}