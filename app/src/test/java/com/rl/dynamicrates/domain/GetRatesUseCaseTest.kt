package com.rl.dynamicrates.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.rl.dynamicrates.common.Try
import com.rl.dynamicrates.sources.RatesApi
import com.rl.dynamicrates.sources.RatesResponse
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import junit.framework.TestCase.fail
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations
import retrofit2.Response

class GetRatesUseCaseTest {

    @Mock
    private lateinit var mockRatesApi: RatesApi

    private lateinit var getRatesUseCase: GetRatesUseCase


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        getRatesUseCase = GetRatesUseCase(mockRatesApi)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mockRatesApi)
    }

    @Test
    fun `return throwable on error`() {
        // given
        val throwable = Throwable("Error")
        val base = ""
        mockEndpointError(throwable)

        // when
        val testObserver = getRatesUseCase.run(base).test()

        // then
        verityRatesApiCall(base)
        extractFirstValueAsTryError(testObserver) { value ->
            testObserver.assertValue(value)
            testObserver.assertComplete()
        }
    }

    @Test
    fun `return throwable with its message on unsuccessful response`() {
        // given
        val base = ""
        val errorMessage = "This is error message"
        val errorResponse = Response.error<RatesResponse>(
            400,
            ResponseBody.create(null, errorMessage)
        )
        mockEndpointSuccess(errorResponse)

        // when
        val testObserver = getRatesUseCase.run(base).test()

        // then
        verityRatesApiCall(base)
        extractFirstValueAsTryError(testObserver) { value ->
            testObserver.assertValueCount(1)
            testObserver.assertComplete()
            assertThat(value.throwable.message, equalTo(errorMessage))
        }
    }

    @Test
    fun `return throwable with predefined message on unsuccessful response with empty body`() {
        // given
        val base = ""
        val errorMessage = "Error without body returned"
        val errorResponse = mock<Response<RatesResponse>>()
        whenever(errorResponse.isSuccessful).thenReturn(false)
        whenever(errorResponse.errorBody()).thenReturn(null)
        mockEndpointSuccess(errorResponse)

        // when
        val testObserver = getRatesUseCase.run(base).test()

        // then
        verityRatesApiCall(base)
        extractFirstValueAsTryError(testObserver) { value ->
            testObserver.assertValueCount(1)
            testObserver.assertComplete()
            assertThat(value.throwable.message, equalTo(errorMessage))
        }
    }

    @Test
    fun `return throwable with predefined message on successful response with empty body`() {
        // given
        val base = ""
        val errorMessage = "Success without body returned"
        mockEndpointSuccess(Response.success(null))

        // when
        val testObserver = getRatesUseCase.run(base).test()

        // then
        verityRatesApiCall(base)
        extractFirstValueAsTryError(testObserver) { value ->
            testObserver.assertValueCount(1)
            testObserver.assertComplete()
            assertThat(value.throwable.message, equalTo(errorMessage))
        }
    }

    @Test
    fun `return RatesEntity on successful response with correct body`() {
        // given
        val currencyRate = "PLN" to 1.23
        val ratesBase = "EUR"
        mockEndpointSuccess(Response.success(RatesResponse(ratesBase, mapOf(currencyRate))))

        // when
        val testObserver = getRatesUseCase.run(ratesBase).test()

        // then
        verityRatesApiCall(ratesBase)
        extractFirstValueAsTrySuccess(testObserver) { value ->
            testObserver.assertComplete()
            assertThat(value.success.base, equalTo(ratesBase))
            assertThat(value.success.rates, equalTo(hashMapOf(currencyRate)))
        }
    }

    private fun mockEndpointError(throwable: Throwable) {
        whenever(mockRatesApi.getRates(anyString()))
            .thenReturn(Single.error(throwable))
    }

    private fun mockEndpointSuccess(errorResponse: Response<RatesResponse>?) {
        whenever(mockRatesApi.getRates(anyString()))
            .thenReturn(Single.just(errorResponse))
    }

    private fun extractFirstValueAsTryError(
        testObserver: TestObserver<Try<RatesEntity>>,
        callback: (Try.Error) -> Unit
    ) {
        val value = testObserver.values().first()
        if (value is Try.Error) {
            callback(value)
        } else {
            fail("Returned value isn't Try.Error")
        }
    }

    private fun extractFirstValueAsTrySuccess(
        testObserver: TestObserver<Try<RatesEntity>>,
        callback: (Try.Success<RatesEntity>) -> Unit
    ) {
        val value = testObserver.values().first()
        if (value is Try.Success<RatesEntity>) {
            callback(value)
        } else {
            fail("Returned value isn't Try.Error")
        }
    }

    private fun verityRatesApiCall(base: String) {
        verify(mockRatesApi).getRates(base)
    }

}
