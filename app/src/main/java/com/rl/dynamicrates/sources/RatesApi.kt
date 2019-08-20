package com.rl.dynamicrates.sources

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface RatesApi {
    @GET("latest?base=EUR")
    fun getRates(@Query("base") base: String): Single<RatesResponse>
}