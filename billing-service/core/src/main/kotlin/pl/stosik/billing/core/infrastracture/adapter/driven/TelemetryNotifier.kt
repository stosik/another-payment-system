package pl.stosik.billing.core.infrastracture.adapter.driven

import pl.stosik.billing.core.extensions.logger.logger
import pl.stosik.billing.core.port.driven.Notifier

class TelemetryNotifier : Notifier {

    private val log by logger()

    override suspend fun notify(message: String) {
        log.info { message }
    }
}