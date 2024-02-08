package pl.stosik.billing.core.port.driver

interface JobScheduler {
    suspend fun schedule()
    fun stop()
}