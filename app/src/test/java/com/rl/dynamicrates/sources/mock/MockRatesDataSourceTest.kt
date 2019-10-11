package com.rl.dynamicrates.sources.mock

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test

class MockRatesDataSourceTest {
    private val fakeDataFetcher = FakeDataFetcher()
    private val mockRatesDataSource = MockRatesDataSource(fakeDataFetcher)

    @Test
    fun `next responses provide rates with change delta not bigger than 0_02`() {
        // given
        val base = eurBase
        prepareFakeData()
        var previousRates = getRatesForBase(base)

        // when
        for (i in 0..100) {
            val newRates = getRatesForBase(base)
            for (rateKey in previousRates.keys) {
                // then
                assertThat(
                    newRates[rateKey],
                    `is`(
                        BetweenNewRandomValuesRangeFor(previousRates[rateKey])
                    )
                )
            }
            previousRates = newRates
        }
    }

    @Test
    fun `rates are calculated properly using previous rates after base change`() {
        // given
        prepareFakeData()

        // when
        val newRates = getRatesForBase(plnBase)

        // then
        assertThat(
            newRates[eurBase],
            `is`(
                BetweenNewRandomValuesRangeFor(0.5)
            )
        )
        assertThat(
            newRates[usdBase],
            `is`(
                BetweenNewRandomValuesRangeFor(0.25)
            )
        )
        assertThat(
            newRates[chfBase],
            `is`(
                BetweenNewRandomValuesRangeFor(0.5)
            )
        )
    }

    private fun getRatesForBase(base: String) =
        mockRatesDataSource.getRates(base).blockingGet().body()!!.rates

    private fun prepareFakeData() {
        fakeDataFetcher.setFakeData(fakeBase, fakeRates)
    }

    companion object {
        private const val fakeBase = "EUR"
        private const val eurBase = "EUR"
        private const val usdBase = "USD"
        private const val chfBase = "CHF"
        private const val plnBase = "PLN"
        private val fakeRates =
            hashMapOf(usdBase to 0.5, chfBase to 1.0, plnBase to 2.0)
    }
}