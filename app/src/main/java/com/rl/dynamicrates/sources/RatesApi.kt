package com.rl.dynamicrates.sources

import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RatesApi {
    @GET("latest")
    fun getRates(@Query("base") base: String): Single<Response<RatesResponse>>
}