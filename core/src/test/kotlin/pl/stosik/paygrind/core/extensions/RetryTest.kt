package pl.stosik.paygrind.core.extensions

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import pl.stosik.paygrind.core.extensions.resilience.retry
import pl.stosik.paygrind.models.domain.errors.AntaeusError
import pl.stosik.paygrind.models.domain.errors.AntaeusError.BillingError.RetryableError.PaymentProviderNetworkError

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RetryExtensionTest {

    private val retryMock = mockk<RetryMock>()

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should retry 3 times and return error back`(): Unit = runTest {
        every { retryMock.mock() } returns PaymentProviderNetworkError.left()

        val result = retry(3, onErrors = arrayOf(PaymentProviderNetworkError)) {
            retryMock.mock()
        }

        result.isLeft() shouldBe true
        result.onLeft { it shouldBe PaymentProviderNetworkError }
        verify(exactly = 3) { retryMock.mock() }
    }

    @Test
    fun `should retry 2 times and return success`(): Unit = runTest {
        every { retryMock.mock() } returnsMany listOf(PaymentProviderNetworkError.left(), Unit.right())

        val result = retry(3, onErrors = arrayOf(PaymentProviderNetworkError)) {
            retryMock.mock()
        }

        result.isRight() shouldBe true
        result.onRight { it shouldBe Unit }
        verify(exactly = 2) { retryMock.mock() }
    }
}

internal class RetryMock {

    fun mock(): Either<AntaeusError, Unit> = Unit.right()
}