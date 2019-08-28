package com.rl.dynamicrates.ui.activity

import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.rl.dynamicrates.RxSchedulersAsTestScheduler
import com.rl.dynamicrates.common.Try
import com.rl.dynamicrates.domain.GetRatesUseCase
import com.rl.dynamicrates.domain.RatesEntity
import com.rl.dynamicrates.ui.models.CurrencyWithFlagModel
import com.rl.dynamicrates.ui.models.RateModel
import io.reactivex.Single
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.TimeUnit

class RatesActivityPresenterTest {

    @get:Rule
    val testSchedulersRule = RxSchedulersAsTestScheduler()

    @Mock
    lateinit var mockGetRatesUseCase: GetRatesUseCase
    @Mock
    lateinit var mockView: RatesActivityContract.View

    private lateinit var presenter: RatesActivityPresenter

    private val initialBaseCurrency = CurrencyWithFlagModel.EUR
    private val initialBaseAmount = 100.0
    private val initialRateModel = RateModel(initialBaseCurrency, initialBaseAmount, isBase = true)
    private val notInitialBaseCurrencyCode = "PLN"

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = RatesActivityPresenter(mockGetRatesUseCase)
        presenter.attachView(mockView)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mockView, mockGetRatesUseCase)
    }

    @Test
    fun `set view on attachView`() {
        // when
        presenter.attachView(mockView)

        // then
        MatcherAssert.assertThat(presenter.view, CoreMatchers.equalTo(mockView))
    }

    @Test
    fun `call nothing in onStop`() {
        // when
        presenter.onStop()

        // then
        // call nothing
    }

    @Test
    fun `call nothing in onDestroy`() {
        // when
        presenter.onDestroy()

        // then
        // call nothing
    }

    @Test
    fun `show progress bar only on first startFetching`() {
        // when
        presenter.onStart()
        presenter.onStart()

        // then
        verify(mockView).showProgressBar()
    }

    @Test
    fun `show error snackbar on getRatesUseCase error`() {
        // given
        val success = Try.Success(RatesEntity(notInitialBaseCurrencyCode, hashMapOf()))
        whenever(mockGetRatesUseCase.run(anyString()))
            .thenReturn(Single.just(Try.Error(Throwable("Error when fetching rates"))))
            .thenReturn(Single.just(success))

        presenter.onStart()
        verify(mockView).showProgressBar()

        // when
        testSchedulersRule.testScheduler.advanceTimeBy(1500, TimeUnit.MILLISECONDS)

        // then
        verify(mockGetRatesUseCase, times(2)).run(anyString())
        verify(mockView).showErrorSnackbar()
        verify(mockView).hideErrorSnackbar()
    }

    @Test
    fun `filter out response for not current base`() {
        // given
        val success = Try.Success(RatesEntity(notInitialBaseCurrencyCode, hashMapOf()))
        whenever(mockGetRatesUseCase.run(anyString()))
            .thenReturn(Single.just(success))

        presenter.onStart()
        verify(mockView).showProgressBar()

        // when
        testSchedulersRule.testScheduler.advanceTimeBy(2, TimeUnit.SECONDS)

        // then
        verify(mockGetRatesUseCase, times(3)).run(anyString())
    }

    @Test
    fun `update rates for current base response`() {
        goThroughOneSuccessfulResponsePath()
    }

    @Test
    fun `do nothing on base currency click`() {
        // given
        goThroughOneSuccessfulResponsePath()

        // when
        presenter.onRateClick(initialRateModel)

        // then
        // do nothing
    }

    @Test
    fun `move clicked currency to top and mark as base on not base currency click`() {
        // given
        val firstCurrencyRate = 1.24
        val secondCurrencyRate = 7.78
        val models = goThroughOneSuccessfulResponsePath(firstCurrencyRate, secondCurrencyRate)

        // when
        presenter.onRateClick(models[1])

        // then
        val updatedModels = arrayListOf(
            RateModel(CurrencyWithFlagModel.AUD, initialBaseAmount * secondCurrencyRate, true),
            initialRateModel.copy(isBase = false),
            RateModel(CurrencyWithFlagModel.PLN, initialBaseAmount * firstCurrencyRate)
        )

        verify(mockView).updateRates(updatedModels)
    }

    @Test
    fun `do nothing on amount of not base currency change`() {
        // given
        val models = goThroughOneSuccessfulResponsePath()

        // when
        presenter.onAmountChange(models[1])

        // then
        // do nothing
    }

    @Test
    fun `change amounts on base currency amount change`() {
        // given
        val firstCurrencyRate = 1.24
        val secondCurrencyRate = 7.78
        goThroughOneSuccessfulResponsePath(firstCurrencyRate, secondCurrencyRate)

        // when
        val newBaseAmount = 10.0
        val newBaseRateModel = initialRateModel.copy(amount = newBaseAmount)
        presenter.onAmountChange(newBaseRateModel)

        // then
        val updatedModels = arrayListOf(
            newBaseRateModel,
            RateModel(CurrencyWithFlagModel.AUD, newBaseAmount * secondCurrencyRate, false),
            RateModel(CurrencyWithFlagModel.PLN, newBaseAmount * firstCurrencyRate)
        )

        verify(mockView).updateRates(updatedModels)
    }

    private fun goThroughOneSuccessfulResponsePath(
        firstCurrencyRate: Double = 1.23,
        secondCurrencyRate: Double = 7.77
    ): ArrayList<RateModel> {
        // given
        val firstCurrency = notInitialBaseCurrencyCode
        val secondCurrency = "AUD"
        val ratesEntityResponse = RatesEntity(
            initialBaseCurrency.currency.currencyCode,
            hashMapOf(firstCurrency to firstCurrencyRate, secondCurrency to secondCurrencyRate)
        )
        whenever(mockGetRatesUseCase.run(anyString()))
            .thenReturn(Single.just(Try.Success(ratesEntityResponse)))

        presenter.onStart()
        verify(mockView).showProgressBar()

        // when
        testSchedulersRule.testScheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS)

        // then
        val models = arrayListOf(
            initialRateModel,
            RateModel(CurrencyWithFlagModel.AUD, initialBaseAmount * secondCurrencyRate),
            RateModel(CurrencyWithFlagModel.PLN, initialBaseAmount * firstCurrencyRate)
        )
        verify(mockGetRatesUseCase).run(anyString())
        verify(mockView).updateRates(models)
        verify(mockView).hideProgressBar()

        return models
    }
}