package pl.stosik.paygrind.data.configuration

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitLast
import pl.stosik.paygrind.models.infrastracture.ApplicationConfiguration
import java.time.Duration
import kotlin.concurrent.fixedRateTimer

class ConnectionPoolBuilderImpl(
    databaseConfig: ApplicationConfiguration.DatabaseConfiguration,
) : ConnectionPoolBuilder {
    override fun createPool(): ConnectionPool = pool

    private val connectionConfig = ConnectionFactories.get(
        ConnectionFactoryOptions
            .parse(databaseConfig.url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, databaseConfig.username)
            .option(ConnectionFactoryOptions.PASSWORD, databaseConfig.password)
            .build()
    )

    private val poolConfig = ConnectionPoolConfiguration.builder(connectionConfig)
        .initialSize(POOL_INITIAL_SIZE)
        .maxSize(POOL_MAX_SIZE)
        .minIdle(POOL_INITIAL_SIZE)
        .maxIdleTime(MAX_IDLE_TIME)
        .backgroundEvictionInterval(IDLE_CONNECTION_TEST_PERIOD)
        .acquireRetry(POOL_MAX_SIZE)
        .build()

    private fun schedulePoolWarmup(pool: ConnectionPool) {
        val warmupScope = CoroutineScope(Dispatchers.Unconfined)

        fixedRateTimer(period = IDLE_CONNECTION_TEST_PERIOD.toMillis()) {
            warmupScope.launch {
                try {
                    pool.warmup().awaitLast()
                } catch (exception: IllegalArgumentException) {
                    // reactor-pool in version <1.0.4 has a bug that determining number of threads that can be used for
                    // connection pool initialisation as minimum of configured concurrency (default 1) or pools for warmup.
                    // On initial run when there are no pools this values is picked as Math.min(1, 10) and later function
                    // of reactor works fine. On subsequent runs when connection pool does not require warmup it does
                    // Math.min(1, 0) and passes the 0 as max thread value for connection pool initialisation.
                    // This bug does not change anything - if warmup is needed it works, if not it fails, so this
                    // exception is completely ignorable.
                }
            }
        }
    }

    private val pool = ConnectionPool(poolConfig).also { schedulePoolWarmup(it) }

    companion object {
        private const val POOL_INITIAL_SIZE = 10
        private const val POOL_MAX_SIZE = 10
        private val IDLE_CONNECTION_TEST_PERIOD = Duration.ofSeconds(30)
        private val MAX_IDLE_TIME = Duration.ofHours(6)
    }
}