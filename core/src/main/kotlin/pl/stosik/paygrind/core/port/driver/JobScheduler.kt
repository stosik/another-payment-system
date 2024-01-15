package pl.stosik.paygrind.core.port.driver

interface JobScheduler {
    suspend fun schedule()
    fun stop()
}