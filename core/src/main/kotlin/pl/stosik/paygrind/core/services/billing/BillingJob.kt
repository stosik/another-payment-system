package pl.stosik.paygrind.core.services.billing

import pl.stosik.paygrind.core.extensions.logger.logger
import pl.stosik.paygrind.core.port.driven.TimeProvider
import pl.stosik.paygrind.core.services.job_lock.JobLockFactory
import pl.stosik.paygrind.core.services.job_lock.JobLockService
import kotlinx.coroutines.runBlocking
import org.quartz.Job
import org.quartz.JobExecutionContext

class BillingJob : Job {

    private val log by logger()

    override fun execute(context: JobExecutionContext): Unit = runBlocking {
        val billingProcessor = context.jobDetail.jobDataMap["billingProcessor"] as BillingProcessor
        val jobLockService = context.jobDetail.jobDataMap["jobLockService"] as JobLockService
        val timeProvider = context.jobDetail.jobDataMap["timeProvider"] as TimeProvider
        val instanceId = context.jobDetail.jobDataMap["instanceId"] as String
        val jobLockName = JobLockFactory.createName(context.jobDetail.key.name, instanceId, timeProvider.now())

        if (jobLockService.acquireLock(jobLockName)) {
            log.info { "Staring pending invoices processing..." }
            billingProcessor.processInvoices()
            jobLockService.releaseLock(jobLockName)
        } else {
            log.info { "Billing job is already running. Skipping..." }
        }
    }
}