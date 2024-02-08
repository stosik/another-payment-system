package pl.stosik.billing.core.infrastracture.adapter.driven

import pl.stosik.billing.core.extensions.logger.logger
import pl.stosik.billing.core.port.driven.Notifier

class EmailNotifier : Notifier {

    private val log by logger()

    override suspend fun notify(message: String) {
        log.info { "Send email through external provider informing about $message" }
    }
}