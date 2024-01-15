package pl.stosik.paygrind.core.infrastracture.adapter.driven

import pl.stosik.paygrind.core.extensions.logger.logger
import pl.stosik.paygrind.core.port.driven.Notifier

class TelemetryNotifier : Notifier {

    private val log by logger()

    override suspend fun notify(message: String) {
        log.info { message }
    }
}