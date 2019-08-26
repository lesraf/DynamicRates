package com.rl.dynamicrates.domain

import com.nhaarman.mockitokotlin2.whenever
import com.rl.dynamicrates.RxSchedulersAsTrampoline
import com.rl.dynamicrates.common.Try
import com.rl.dynamicrates.sources.RatesApi
import io.reactivex.Single
import junit.framework.TestCase.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.MockitoAnnotations

class GetRatesUseCaseTest {

    @get:Rule
    val rule = RxSchedulersAsTrampoline()

    @Mock
    private lateinit var mockRatesApi: RatesApi

    private lateinit var getRatesUseCase: GetRatesUseCase


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        verifyNoMoreInteractions(mockRatesApi)

        getRatesUseCase = GetRatesUseCase(mockRatesApi)
    }

    @Test
    fun `on error return that throwable`() {
        // given
        val throwable = Throwable("Error")
        whenever(mockRatesApi.getRates(anyString()))
            .thenReturn(Single.error(throwable))

        // whenK
        val testObserver = getRatesUseCase.run("").test()

        // then
        val value = testObserver.values().first()
        if (value is Try.Error) {
            testObserver.assertValue(value)
            testObserver.assertComplete()
        } else {
            fail("Returned value isn't Try.Error")
        }
    }
}