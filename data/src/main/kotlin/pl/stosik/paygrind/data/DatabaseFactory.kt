package pl.stosik.paygrind.data

import arrow.fx.coroutines.ResourceScope
import io.r2dbc.pool.ConnectionPool
import org.jooq.DSLContext
import org.jooq.impl.DSL
import pl.stosik.paygrind.data.configuration.ConnectionPoolBuilderImpl
import pl.stosik.paygrind.models.infrastracture.ApplicationConfiguration.DatabaseConfiguration

suspend fun ResourceScope.jooq(configuration: DatabaseConfiguration): JooqEngine {
    val connectionPool = connectionPool(configuration)
    val dsl = dsl(connectionPool)

    return JooqEngine(dsl)
}

private suspend fun ResourceScope.connectionPool(configuration: DatabaseConfiguration): ConnectionPool = install({
    ConnectionPoolBuilderImpl(configuration).createPool()
}) { p, _ -> p.close() }

private fun dsl(connectionPool: ConnectionPool): DSLContext = DSL.using(connectionPool)