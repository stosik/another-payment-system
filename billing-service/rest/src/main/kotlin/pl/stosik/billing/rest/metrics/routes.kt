package pl.stosik.billing.rest.metrics

import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry

@Resource("/metrics")
class Metrics

fun Routing.metricsRoute(metrics: PrometheusMeterRegistry) =
    get<Metrics> { call.respond(metrics.scrape()) }
