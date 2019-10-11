package com.rl.dynamicrates.sources.mock

import com.rl.dynamicrates.sources.RatesApi
import com.rl.dynamicrates.sources.RatesResponse
import com.squareup.moshi.Moshi
import io.reactivex.Single
import retrofit2.Response
import kotlin.random.Random

class MockRatesDataSource(private val mockDataFetcher: DataFetcher) : RatesApi {

    private val moshi: Moshi = Moshi.Builder().build()
    private val random = Random(System.currentTimeMillis())
    private var previousBase = eur

    private var currentRatesForEur = mapOf<String, Double>()

    override fun getRates(base: String): Single<Response<RatesResponse>> {
        if (currentRatesForEur.isEmpty()) currentRatesForEur = readMockedResponse().rates
        if (base == previousBase) {
            currentRatesForEur = calculateNewCurrentRates()
        }
        previousBase = base

        val ratesForBase = calculateNewRatesForBase(base)
        val response = Response.success(RatesResponse(base, ratesForBase))
        return Single.just(response)
    }

    private fun readMockedResponse(): RatesResponse {
        val mockedResponseText = mockDataFetcher.fetchData()
        val mockedResponse =
            moshi.adapter(RatesResponse::class.java)
                .fromJson(mockedResponseText)

        requireNotNull(mockedResponse)

        return mockedResponse
    }

    private fun calculateNewCurrentRates(): Map<String, Double> {
        val newRates = hashMapOf<String, Double>()
        currentRatesForEur.map { entry ->
            val minMultiplier = 1 - responseDelta
            val maxMultiplier = 1 + responseDelta
            newRates[entry.key] = entry.value * random.nextDouble(minMultiplier, maxMultiplier)
        }
        return newRates.toMap()
    }

    private fun calculateNewRatesForBase(base: String): Map<String, Double> {
        if (base == eur) return currentRatesForEur

        val currentBaseRateToEur = currentRatesForEur[base]
        checkNotNull(currentBaseRateToEur)

        val newRates = hashMapOf<String, Double>()
        for (rate in currentRatesForEur) {
            if (rate.key != base) {
                newRates[rate.key] = rate.value / currentBaseRateToEur
            }
        }
        newRates[eur] = 1 / currentBaseRateToEur

        return newRates.toMap()
    }

    companion object {
        private const val eur = "EUR"
        private const val responseDelta = 0.02
    }
}