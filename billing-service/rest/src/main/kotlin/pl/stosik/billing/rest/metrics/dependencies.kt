package pl.stosik.billing.rest.metrics

import arrow.fx.coroutines.ResourceScope
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.prometheus.client.Gauge

suspend fun ResourceScope.metricsRegistry(): PrometheusMeterRegistry =
    install({ PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }) { p, _ -> p.close() }

fun invoicesToProcessGauge(meterRegistry: PrometheusMeterRegistry): Gauge =
    Gauge
        .build()
        .name("invoices_to_process_gauge")
        .help("Number of invoices to be processed for billing.")
        .register(meterRegistry.prometheusRegistry)
