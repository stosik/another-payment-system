package pl.stosik.billing.app

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import com.sksamuel.cohort.HealthCheck
import com.sksamuel.cohort.HealthCheckRegistry
import com.sksamuel.cohort.hikari.HikariConnectionsHealthCheck
import com.sksamuel.cohort.kafka.KafkaClusterHealthCheck
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.Dispatchers
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import pl.stosik.billing.core.infrastracture.adapter.driven.ChargeInvoiceSource
import pl.stosik.billing.core.infrastracture.adapter.driven.CurrentTimeProvider
import pl.stosik.billing.core.infrastracture.adapter.driven.EmailNotifier
import pl.stosik.billing.core.infrastracture.adapter.driven.FixedCurrencyProvider
import pl.stosik.billing.core.infrastracture.adapter.driven.RandomPaymentProvider
import pl.stosik.billing.core.infrastracture.adapter.driven.TelemetryNotifier
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
import pl.stosik.billing.data.hikari
import pl.stosik.billing.models.infrastracture.ApplicationConfiguration
import pl.stosik.billing.models.infrastracture.joinKeysFlattening
import pl.stosik.billing.rest.metrics.metricsRegistry
import pl.stosik.messaging.kafka.KafkaConfiguration
import kotlin.time.Duration.Companion.seconds

class Dependencies(
    val jobLockService: JobLockService,
    val invoiceService: InvoiceService,
    val customerService: CustomerService,
    val billingService: BillingService,
    val billingProcessor: BillingProcessor,
    val healthCheckRegistry: HealthCheckRegistry,
    val metricsRegistry: PrometheusMeterRegistry,
    val timeProvider: TimeProvider
)

suspend fun ResourceScope.dependencies(configuration: ApplicationConfiguration): Dependencies {
    val dataSource = hikari(configuration = configuration.database)
    val exposedEngine = exposed(dataSource = dataSource)

    // Metrics
    val metricsRegistry = metricsRegistry()

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

    // Kafka
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

    // Healthchecks
    val kafkaHealthCheck = kafkaHealthCheck(configuration.kafka)
    val healthCheckRegistry = HealthCheckRegistry(Dispatchers.Default) {
        register(HikariConnectionsHealthCheck(dataSource, 1), 5.seconds)
        register(kafkaHealthCheck, 5.seconds)
    }

    return Dependencies(
        jobLockService = jobLockService,
        invoiceService = invoiceService,
        customerService = customerService,
        billingService = billingService,
        billingProcessor = billingProcessor,
        healthCheckRegistry = healthCheckRegistry,
        metricsRegistry = metricsRegistry,
        timeProvider = CurrentTimeProvider()
    )
}

context (ResourceScope) suspend fun kafkaHealthCheck(env: ApplicationConfiguration.KafkaConfiguration): HealthCheck =
    KafkaClusterHealthCheck(autoCloseable {
        AdminClient.create(mapOf(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to env.consumer.bootstrap.servers))
    })
