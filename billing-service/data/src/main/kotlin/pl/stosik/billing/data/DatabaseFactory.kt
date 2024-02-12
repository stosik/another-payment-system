package pl.stosik.billing.data

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.autoCloseable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import pl.stosik.billing.data.adapter.driven.CustomerTable
import pl.stosik.billing.data.adapter.driven.InvoiceTable
import pl.stosik.billing.data.adapter.driven.JobLockTable
import pl.stosik.billing.models.infrastracture.ApplicationConfiguration.DatabaseConfiguration
import java.sql.Connection
import javax.sql.DataSource

private val tables = arrayOf(InvoiceTable, CustomerTable, JobLockTable)

suspend fun ResourceScope.hikari(configuration: DatabaseConfiguration): HikariDataSource = autoCloseable {
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = configuration.url
            driverClassName = "org.postgresql.Driver"
            username = configuration.username
            password = configuration.password
            maximumPoolSize = 10
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
        }
    )
}

suspend fun ResourceScope.exposed(dataSource: DataSource): Database {
    return install({
        Database
            .connect(dataSource)
            .apply {
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                transaction(this) {
                    addLogger(StdOutSqlLogger)
                    SchemaUtils.drop(*tables)
                    SchemaUtils.create(*tables)
                }
            }
    }) { p, _ -> TransactionManager.closeAndUnregister(p) }
}