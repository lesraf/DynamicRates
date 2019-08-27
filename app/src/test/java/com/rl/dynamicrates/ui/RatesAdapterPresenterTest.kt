package com.rl.dynamicrates.ui

import androidx.recyclerview.widget.DiffUtil
import com.nhaarman.mockitokotlin2.*
import com.rl.dynamicrates.RxSchedulersAsTrampoline
import com.rl.dynamicrates.ui.models.CurrencyWithFlagModel
import com.rl.dynamicrates.ui.models.RateModel
import io.reactivex.Single
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class RatesAdapterPresenterTest {

    @get:Rule
    val schedulersRule = RxSchedulersAsTrampoline()

    @Mock
    private lateinit var mockView: RatesAdapterContract.View
    @Mock
    private lateinit var mockCalculateRatesDiffUseCase: CalculateRatesDiffUseCase

    private lateinit var presenter: RatesAdapterPresenter

    private val rateModels =
        listOf(
            RateModel(CurrencyWithFlagModel.AUD, 1.23),
            RateModel(CurrencyWithFlagModel.PLN, 7.77)
        )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = RatesAdapterPresenter(mockCalculateRatesDiffUseCase)
    }

    @After
    fun tearDown() {
        verifyNoMoreInteractions(mockView, mockCalculateRatesDiffUseCase)
    }

    @Test
    fun `set view in attachView`() {
        // when
        presenter.attachView(view = mockView)

        // then
        assertThat(presenter.view, equalTo(mockView))
    }

    @Test
    fun `null view in onDestroy`() {
        // when
        presenter.onDestroy()

        // then
        assertThat(presenter.view, `is`(nullValue()))
    }

    @Test
    fun `dispatch updates on onUpdate`() {
        // given
        val diffResult = mockCalculateRatesDiffResult()
        presenter.attachView(mockView)

        // when
        presenter.onUpdate(rateModels)

        // then
        verifyOnUpdateCall(diffResult)
    }

    @Test
    fun `return correct itemCount on onGetItemCount`() {
        // given
        val diffResult = mockCalculateRatesDiffResult()
        presenter.attachView(mockView)
        presenter.onUpdate(rateModels)

        verifyOnUpdateCall(diffResult)

        // when
        val itemCount = presenter.onGetItemCount()

        // then
        assertThat(itemCount, equalTo(rateModels.size))
    }

    @Test
    fun `return correct item on onBindViewHolder with position parameter`() {
        // given
        val diffResult = mockCalculateRatesDiffResult()
        presenter.attachView(mockView)
        presenter.onUpdate(rateModels)

        verifyOnUpdateCall(diffResult)

        // when
        val chosenPosition = 0
        val returnedRateModel = presenter.onBindViewHolder(chosenPosition)

        // then
        assertThat(returnedRateModel, equalTo(rateModels[chosenPosition]))
    }

    @Test
    fun `return null for empty payloads`() {
        // given

        // when
        val result = presenter.onBindViewHolder(mutableListOf())

        // then
        assertThat(result, `is`(nullValue()))
    }

    @Test
    fun `return last payload for from payloads list`() {
        // given
        val firstPayload = RateViewHolder.PayloadChange(1.23, false)
        val lastPayload = RateViewHolder.PayloadChange(7.77, true)
        val payloads: MutableList<Any> = mutableListOf(firstPayload, lastPayload)

        // when
        val result = presenter.onBindViewHolder(payloads)

        // then
        assertThat(result, equalTo(lastPayload))
    }

    private fun mockCalculateRatesDiffResult(): DiffUtil.DiffResult {
        val diffResult = mock<DiffUtil.DiffResult>()
        whenever(mockCalculateRatesDiffUseCase.run(any(), any()))
            .thenReturn(Single.just(diffResult))
        return diffResult
    }

    private fun verifyOnUpdateCall(diffResult: DiffUtil.DiffResult) {
        verify(mockCalculateRatesDiffUseCase).run(any(), any())
        verify(mockView).dispatchUpdates(diffResult)
    }

}
