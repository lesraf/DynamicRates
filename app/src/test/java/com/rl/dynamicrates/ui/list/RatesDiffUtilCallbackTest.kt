package com.rl.dynamicrates.ui.list

import com.rl.dynamicrates.ui.list.viewholder.RateViewHolder
import com.rl.dynamicrates.ui.models.CurrencyWithFlagModel
import com.rl.dynamicrates.ui.models.RateModel
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class RatesDiffUtilCallbackTest {
    @Test
    fun `areItemsTheSame returns true for same currencyWithFlagModel`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.GBP, 1.23, isBase = true)),
            listOf(RateModel(CurrencyWithFlagModel.GBP, 7.23, isBase = false))
        )

        // when
        val result = ratesDiffUtilCallback.areItemsTheSame(0, 0)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `areItemsTheSame returns false for different currencyWithFlagModel`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.GBP, 1.23, isBase = true)),
            listOf(RateModel(CurrencyWithFlagModel.PLN, 1.23, isBase = true))
        )

        // when
        val result = ratesDiffUtilCallback.areItemsTheSame(0, 0)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `getOldListSize returns old list size`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.GBP, 1.23)),
            listOf()
        )

        // when
        val result = ratesDiffUtilCallback.oldListSize

        // then
        assertThat(result, equalTo(1))
    }

    @Test
    fun `getNewListSize returns new list size`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.GBP, 1.23)),
            listOf()
        )

        // when
        val result = ratesDiffUtilCallback.newListSize

        // then
        assertThat(result, equalTo(0))
    }

    @Test
    fun `areContentsTheSame returns true for the same contents`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.GBP, 1.23)),
            listOf(RateModel(CurrencyWithFlagModel.GBP, 1.23))
        )

        // when
        val result = ratesDiffUtilCallback.areContentsTheSame(0, 0)

        // then
        assertThat(result, equalTo(true))
    }

    @Test
    fun `areContentsTheSame returns true for different currency`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.PLN, 1.23)),
            listOf(RateModel(CurrencyWithFlagModel.GBP, 1.23))
        )

        // when
        val result = ratesDiffUtilCallback.areContentsTheSame(0, 0)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `areContentsTheSame returns true for different amount`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.PLN, 1.23)),
            listOf(RateModel(CurrencyWithFlagModel.PLN, 0.0))
        )

        // when
        val result = ratesDiffUtilCallback.areContentsTheSame(0, 0)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `areContentsTheSame returns true for different isBase`() {
        // given
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.PLN, 1.23)),
            listOf(RateModel(CurrencyWithFlagModel.PLN, 1.23, isBase = true))
        )

        // when
        val result = ratesDiffUtilCallback.areContentsTheSame(0, 0)

        // then
        assertThat(result, equalTo(false))
    }

    @Test
    fun `getChangePayload returns new item's values`() {
        // given
        val newItemsAmount = 0.0
        val newItemsIsBase = true
        val ratesDiffUtilCallback = RatesDiffUtilCallback(
            listOf(RateModel(CurrencyWithFlagModel.PLN, 1.23)),
            listOf(RateModel(CurrencyWithFlagModel.PLN, newItemsAmount, isBase = newItemsIsBase))
        )

        // when
        val result = ratesDiffUtilCallback.getChangePayload(0, 0)

        // then
        assertThat(
            result as RateViewHolder.PayloadChange,
            equalTo(RateViewHolder.PayloadChange(newItemsAmount, newItemsIsBase))
        )
    }
}