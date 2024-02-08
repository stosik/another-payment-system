package pl.stosik.billing.data.port.driven

interface JobLockAcquirer {
    suspend fun acquire(name: String): Boolean
    suspend fun release(name: String): Boolean
    suspend fun releaseAllInstanceLocks(instanceId: String): Boolean
}