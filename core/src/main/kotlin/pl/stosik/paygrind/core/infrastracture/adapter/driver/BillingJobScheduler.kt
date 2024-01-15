package pl.stosik.paygrind.core.infrastracture.adapter.driver

import arrow.fx.coroutines.ResourceScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import pl.stosik.paygrind.core.port.driven.TimeProvider
import pl.stosik.paygrind.core.port.driver.JobScheduler
import pl.stosik.paygrind.core.services.billing.BillingJob
import pl.stosik.paygrind.core.services.billing.BillingProcessor
import pl.stosik.paygrind.core.services.job_lock.JobLockService
import pl.stosik.paygrind.models.infrastracture.ApplicationConfiguration.CronConfiguration

suspend fun ResourceScope.billingJobScheduler(
    cronConfiguration: CronConfiguration,
    billingProcessor: BillingProcessor,
    jobLockService: JobLockService,
    timeProvider: TimeProvider
): BillingJobScheduler = install({
    BillingJobScheduler(
        cronConfiguration = cronConfiguration,
        billingProcessor = billingProcessor,
        jobLockService = jobLockService,
        timeProvider = timeProvider
    )
}) { p, _ -> p.stop() }

class BillingJobScheduler(
    private val cronConfiguration: CronConfiguration,
    billingProcessor: BillingProcessor,
    private val jobLockService: JobLockService,
    timeProvider: TimeProvider
) : JobScheduler {

    private val billingJob = JobBuilder
        .newJob()
        .ofType(BillingJob::class.java)
        .withIdentity(JOB_NAME, JOB_GROUP)
        .usingJobData(
            JobDataMap(
                mapOf(
                    "billingProcessor" to billingProcessor,
                    "jobLockService" to jobLockService,
                    "timeProvider" to timeProvider,
                    "instanceId" to cronConfiguration.instanceId
                )
            )
        ).build()

    private val billingJobTrigger = TriggerBuilder
        .newTrigger()
        .withIdentity(JOB_TRIGGER_NAME, JOB_GROUP)
        .withSchedule(CronScheduleBuilder.cronSchedule(cronConfiguration.expression ?: JOB_SCHEDULE))
        .forJob(JOB_NAME, JOB_GROUP)
        .build()

    private val scheduler = StdSchedulerFactory().scheduler

    override suspend fun schedule(): Unit = coroutineScope {
        launch {
            if (!scheduler.isStarted) {
                scheduler.run {
                    start()
                    scheduleJob(billingJob, billingJobTrigger)
                }
            } else {
                scheduler.rescheduleJob(TriggerKey.triggerKey(JOB_TRIGGER_NAME), billingJobTrigger)
            }
        }
    }

    override fun stop(): Unit = runBlocking {
        scheduler.shutdown()
        jobLockService.releaseAllInstanceLocks(cronConfiguration.instanceId)
    }

    companion object {
        private const val JOB_GROUP = "paygrind"
        private const val JOB_NAME = "billing-job"
        private const val JOB_TRIGGER_NAME = "billing-job-trigger"
        private const val JOB_SCHEDULE = "0 0 0 1 * ?"
    }
}