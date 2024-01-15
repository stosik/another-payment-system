package pl.stosik.paygrind.core.services.job_lock

import pl.stosik.paygrind.data.port.driven.JobLockAcquirer

class JobLockService(private val jobLockAcquirer: JobLockAcquirer) {

    suspend fun acquireLock(jobLockName: String): Boolean {
        return jobLockAcquirer.acquire(jobLockName)
    }

    suspend fun releaseLock(jobLockName: String): Boolean {
        return jobLockAcquirer.release(jobLockName)
    }

    suspend fun releaseAllInstanceLocks(instanceId: String): Boolean {
        return jobLockAcquirer.releaseAllInstanceLocks(instanceId)
    }
}