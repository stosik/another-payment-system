package pl.stosik.paygrind.core.port.driven

interface Notifier {

    suspend fun notify(message: String)
}