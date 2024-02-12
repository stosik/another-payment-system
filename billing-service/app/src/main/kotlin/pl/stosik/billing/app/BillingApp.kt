@file:JvmName("PaygrindApp")

package pl.stosik.billing.app

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import pl.stosik.billing.core.infrastracture.adapter.driver.billingJobScheduler
import pl.stosik.billing.rest.billingServer

fun main() = SuspendApp {
    val config = parseConfiguration(this::class.java.classLoader.getResourceAsStream("application.yaml"))

    resourceScope {
        val dependencies = dependencies(configuration = config)

        server(Netty, host = "127.0.0.1", port = 8080) {
            billingServer(
                invoiceService = dependencies.invoiceService,
                customerService = dependencies.customerService,
                billingService = dependencies.billingService,
                healthCheck = dependencies.healthCheckRegistry,
                metricsRegistry = dependencies.metricsRegistry
            )
        }

        billingJobScheduler(
            cronConfiguration = config.cron,
            billingProcessor = dependencies.billingProcessor,
            jobLockService = dependencies.jobLockService,
            timeProvider = dependencies.timeProvider
        ).schedule()

        awaitCancellation()
    }
}
