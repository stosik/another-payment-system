package pl.stosik.paygrind.core.services.job_lock

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JobLockFactoryTest {

    private val jobLockFactory = JobLockFactory

    @Test
    fun `should return job lock name with year and month`() {
        //given
        val jobName = "job-name"
        val executionTime = LocalDateTime.of(2021, 1, 1, 0, 0, 0)
        val instanceId = "app-1"

        //when
        val jobLockName = jobLockFactory.createName(jobName, instanceId, executionTime)

        //then
        jobLockName shouldBe "job-name-app-1-2021-JANUARY"
    }
}