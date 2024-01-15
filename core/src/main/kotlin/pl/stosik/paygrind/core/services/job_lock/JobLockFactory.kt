package pl.stosik.paygrind.core.services.job_lock

import java.time.LocalDateTime

object JobLockFactory {
    fun createName(jobName: String, instanceId: String, executionTime: LocalDateTime): String {
        val year = executionTime.year.toString()
        val month = executionTime.month.name

        return "$jobName-$instanceId-$year-$month"
    }
}