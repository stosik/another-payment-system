package pl.stosik.billing.core.port.driven

interface Notifier {

    suspend fun notify(message: String)
}