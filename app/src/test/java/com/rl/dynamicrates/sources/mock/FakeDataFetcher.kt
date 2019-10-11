package com.rl.dynamicrates.sources.mock

import com.rl.dynamicrates.sources.RatesResponse
import com.squareup.moshi.Moshi

class FakeDataFetcher : DataFetcher {
    private val moshi = Moshi.Builder().build()

    private lateinit var base: String
    private lateinit var rates: HashMap<String, Double>

    override fun fetchData(): String {
        return moshi.adapter(RatesResponse::class.java)
            .toJson(
                RatesResponse(
                    base,
                    rates
                )
            )
    }

    fun setFakeData(base: String, rates: HashMap<String, Double>) {
        this.base = base
        this.rates = rates
    }
}