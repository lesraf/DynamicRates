package com.rl.dynamicrates.ui

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.rl.dynamicrates.ui.models.CurrencyWithFlagModel
import com.rl.dynamicrates.ui.models.RateModel
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.text.DecimalFormat

class RateViewHolderPresenterTest {

    private val correctAmountFormat = DecimalFormat("#.##")

    @Mock
    lateinit var mockOnRateClickListener: OnRateClickListener
    @Mock
    lateinit var mockOnAmountChangeListener: OnAmountChangeListener
    @Mock
    lateinit var mockView: RateViewHolderContract.View

    private lateinit var presenter: RateViewHolderPresenter

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        presenter =
            RateViewHolderPresenter(mockOnRateClickListener, mockOnAmountChangeListener)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mockView, mockOnRateClickListener, mockOnAmountChangeListener)
    }

    @Test
    fun `set view on attachView`() {
        // when
        presenter.attachView(mockView)

        // then
        MatcherAssert.assertThat(presenter.view, CoreMatchers.equalTo(mockView))
    }

    @Test
    fun `populate views and set listeners on update call for not base currency and non-zero amount`() {
        // given
        val updatedAmount = 4.21
        val rateModel = RateModel(CurrencyWithFlagModel.PLN, 4.21)
        presenter.attachView(mockView)

        // when
        presenter.update(rateModel)

        // then
        verifyPopulateCurrencyCall(rateModel)
        verify(mockView).populateAmountWithoutSelectionSave(correctAmountFormat.format(updatedAmount))
        verify(mockView).setOnClickListeners()
    }

    @Test
    fun `populate views and set listeners on update call for base currency and zero amount`() {
        goThroughUpdateCallPath()
    }

    @Test
    fun `populate amount and add text watcher on updatePayload call for non-zero amount and just set base currency`() {
        // given
        mockGetSelectionStart()
        val rateModel = RateModel(CurrencyWithFlagModel.PLN, 0.0)
        presenter.attachView(mockView)
        presenter.update(rateModel)
        verifyPopulateCurrencyCall(rateModel)
        verify(mockView).populateAmountWithoutSelectionSave("")
        verify(mockView).setOnClickListeners()

        val updatedAmount = 400.0
        val payloadChange = RateViewHolder.PayloadChange(updatedAmount, true)

        // when
        presenter.updatePayload(payloadChange)

        // then
        verify(mockView).populateAmountWithSelectionSave(
            correctAmountFormat.format(updatedAmount),
            0
        )
        verify(mockView).addTextChangedWatcher()
        verify(mockView).getSelectionStart()
    }

    @Test
    fun `populate amount and remove text watcher on updatePayload call for non-zero amount and just set not base currency`() {
        // given
        goThroughUpdateCallPath()

        val updatedAmount = 400.0
        val payloadChange = RateViewHolder.PayloadChange(updatedAmount, false)

        // when
        presenter.updatePayload(payloadChange)

        // then
        verify(mockView).populateAmountWithoutSelectionSave(correctAmountFormat.format(updatedAmount))
        verify(mockView).removeTextChangedWatcher()
    }

    @Test
    fun `invoke onRateClickListener passing rateModel on rate click`() {
        // given
        val rateModel = goThroughUpdateCallPath()

        // when
        presenter.onRateClick()

        // then
        verify(mockOnRateClickListener).invoke(rateModel)
    }

    @Test
    fun `invoke onAmountChangeListener passing changed amount on amount change to non-empty amount`() {
        // given
        val rateModel = goThroughUpdateCallPath()

        // when
        presenter.onAmoutChange("1.99")

        // then
        verify(mockOnAmountChangeListener).invoke(rateModel.copy(amount = 1.99))
    }

    @Test
    fun `invoke onAmountChangeListener passing 0 amount on amount change to empty amount`() {
        // given
        val rateModel = goThroughUpdateCallPath()

        // when
        presenter.onAmoutChange("")

        // then
        verify(mockOnAmountChangeListener).invoke(rateModel.copy(amount = 0.0))
    }

    @Test
    fun `keep selection after amount update on digit add`() {
        // given
        mockGetSelectionStart()
        val rateModel = RateModel(CurrencyWithFlagModel.PLN, 10.0)
        presenter.attachView(mockView)
        presenter.update(rateModel)
        verifyPopulateCurrencyCall(rateModel)
        verify(mockView).populateAmountWithoutSelectionSave(correctAmountFormat.format(rateModel.amount))
        verify(mockView).setOnClickListeners()

        val updatedAmount = 100.0
        val payloadChange = RateViewHolder.PayloadChange(updatedAmount, true)

        // when
        presenter.updatePayload(payloadChange)

        // then
        verify(mockView).populateAmountWithSelectionSave(
            correctAmountFormat.format(updatedAmount),
            0
        )
        verify(mockView).addTextChangedWatcher()
        verify(mockView).getSelectionStart()
    }

    @Test
    fun `move selection start to previous digit after amount update on last digit delete`() {
        // given
        mockGetSelectionStart(selectionStart = 6)
        val initialAmount = 100.12
        val rateModel = RateModel(CurrencyWithFlagModel.PLN, initialAmount)
        presenter.attachView(mockView)
        presenter.update(rateModel)
        verifyPopulateCurrencyCall(rateModel)
        verify(mockView).populateAmountWithoutSelectionSave(correctAmountFormat.format(rateModel.amount))
        verify(mockView).setOnClickListeners()

        val updatedAmount = 100.1
        val payloadChange = RateViewHolder.PayloadChange(updatedAmount, true)

        // when
        presenter.updatePayload(payloadChange)

        // then
        verify(mockView).populateAmountWithSelectionSave(
            correctAmountFormat.format(updatedAmount),
            5
        )
        verify(mockView).addTextChangedWatcher()
        verify(mockView).getSelectionStart()
    }

    private fun verifyPopulateCurrencyCall(rateModel: RateModel) {
        with(rateModel.currencyWithFlagModel) {
            verify(mockView).populateCurrency(
                flagRes,
                currency.currencyCode,
                currency.displayName
            )
        }
    }

    private fun mockGetSelectionStart(selectionStart: Int = 0) {
        whenever(mockView.getSelectionStart()).thenReturn(selectionStart)
    }

    private fun goThroughUpdateCallPath(): RateModel {
        mockGetSelectionStart()
        val rateModel = RateModel(CurrencyWithFlagModel.PLN, 0.0, isBase = true)
        presenter.attachView(mockView)
        presenter.update(rateModel)

        verifyPopulateCurrencyCall(rateModel)
        verify(mockView).populateAmountWithSelectionSave("", 0)
        verify(mockView).addTextChangedWatcher()
        verify(mockView).setOnClickListeners()
        verify(mockView).getSelectionStart()
        return rateModel
    }
}
