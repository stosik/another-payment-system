package pl.stosik.paygrind.data.integration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import pl.stosik.paygrind.data.adapter.driven.*
import pl.stosik.paygrind.data.port.driven.CustomerFinder
import pl.stosik.paygrind.data.port.driven.InvoiceFinder
import pl.stosik.paygrind.data.port.driven.InvoiceUpdater
import pl.stosik.paygrind.data.port.driven.JobLockAcquirer
import pl.stosik.paygrind.models.domain.Currency
import pl.stosik.paygrind.models.domain.Customer
import pl.stosik.paygrind.models.domain.InvoiceStatus
import pl.stosik.paygrind.models.domain.Money
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import pl.stosik.paygrind.data.adapter.driven.CustomerRepository
import pl.stosik.paygrind.data.adapter.driven.CustomerTable

@Testcontainers
open class IntegrationBase {

    private val tables = arrayOf(InvoiceTable, CustomerTable, JobLockTable)

    private val hikariDataSource = HikariConfig().apply {
        jdbcUrl = postgresTestContainer.jdbcUrl
        driverClassName = "org.postgresql.Driver"
        username = postgresTestContainer.username
        password = postgresTestContainer.password
        maximumPoolSize = 10
        transactionIsolation = "TRANSACTION_SERIALIZABLE"
    }.let { HikariDataSource(it) }

    private val db = Database.connect(hikariDataSource)

    private val invoiceRepository = InvoiceRepository(db)
    private val customerRepository = CustomerRepository(db)
    protected val invoiceFinder: InvoiceFinder by lazy { invoiceRepository }
    protected val invoiceUpdater: InvoiceUpdater by lazy { invoiceRepository }
    protected val customerFinder: CustomerFinder by lazy { customerRepository }
    protected val jobLockAcquirer: JobLockAcquirer = JobLockRepository(db)

    companion object {

        @JvmStatic
        @Container
        private val postgresTestContainer = PostgreSQLContainer<Nothing>("postgres:15.2")
    }

    @BeforeEach
    fun setUp() {
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(*tables)
            SchemaUtils.create(*tables)
        }
    }

    protected fun createCustomer(currency: Currency) = customerRepository.createCustomer(currency)!!

    protected fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus) =
        invoiceRepository.createInvoice(amount, customer, status)!!

}