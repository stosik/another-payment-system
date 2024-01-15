package pl.stosik.paygrind.data.port.driven

interface JobLockAcquirer {
    suspend fun acquire(name: String): Boolean
    suspend fun release(name: String): Boolean
    suspend fun releaseAllInstanceLocks(instanceId: String): Boolean
}