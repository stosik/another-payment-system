package pl.stosik.paygrind.core.services.job_lock

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.stosik.paygrind.data.port.driven.JobLockAcquirer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class JobLockServiceTest {

    private val jobLockAcquirer = mockk<JobLockAcquirer>()
    private val jobLockService = JobLockService(jobLockAcquirer = jobLockAcquirer)

    @Test
    fun `should return true when lock is acquired`(): Unit = runTest {
        //given
        val jobLockName = "job-lock-name"
        coEvery { jobLockAcquirer.acquire(jobLockName) } returns true

        //when
        val lockAcquired = jobLockService.acquireLock(jobLockName)

        //then
        lockAcquired shouldBe true
    }

    @Test
    fun `should return false when lock is not acquired`(): Unit = runTest {
        //given
        val jobLockName = "job-lock-name"
        coEvery { jobLockAcquirer.acquire(jobLockName) } returns false

        //when
        val lockAcquired = jobLockService.acquireLock(jobLockName)

        //then
        lockAcquired shouldBe false
    }

    @Test
    fun `should return true when lock could be released`(): Unit = runTest {
        //given
        val jobLockName = "job-lock-name"
        coEvery { jobLockAcquirer.release(jobLockName) } returns true

        //when
        val lockAcquired = jobLockService.releaseLock(jobLockName)

        //then
        lockAcquired shouldBe true
    }

    @Test
    fun `should return true when all lock of running service instance were released`(): Unit = runTest {
        //given
        val appInstance = "app-1"
        coEvery { jobLockAcquirer.releaseAllInstanceLocks(appInstance) } returns true

        //when
        val locksReleased = jobLockService.releaseAllInstanceLocks(appInstance)

        //then
        locksReleased shouldBe true
    }
}