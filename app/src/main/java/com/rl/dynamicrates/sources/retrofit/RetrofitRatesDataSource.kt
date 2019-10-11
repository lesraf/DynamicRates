package com.rl.dynamicrates.sources.retrofit

import com.rl.dynamicrates.sources.RatesApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class RetrofitRatesDataSource @Inject constructor() {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val ratesApi: RatesApi = retrofit.create(RatesApi::class.java)
}