package pl.stosik.billing.data.integration.repositories

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import pl.stosik.billing.data.integration.IntegrationBase

internal class JobLockRepositoryIntegrationTest : IntegrationBase() {

    @Test
    fun `should acquire lock`(): Unit = runTest {
        // Given
        val jobLockName = "job-lock-name"

        // When
        val lockAcquired = jobLockAcquirer.acquire(jobLockName)

        // Then
        lockAcquired shouldBe true
    }

    @Test
    fun `should release lock`(): Unit = runTest {
        // Given
        val jobLockName = "job-lock-name"
        jobLockAcquirer.acquire(jobLockName)

        // When
        val lockReleased = jobLockAcquirer.release(jobLockName)

        // Then
        lockReleased shouldBe true
    }

    @Test
    fun `should release all locks of running service instance`(): Unit = runTest {
        // Given
        val appInstance = "app-1"
        jobLockAcquirer.acquire("job-lock-one-$appInstance")
        jobLockAcquirer.acquire("job-lock-two-$appInstance")

        // When
        val lockReleased = jobLockAcquirer.releaseAllInstanceLocks(appInstance)

        // Then
        lockReleased shouldBe true
    }
}