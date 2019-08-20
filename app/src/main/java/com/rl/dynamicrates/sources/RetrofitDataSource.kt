package com.rl.dynamicrates.sources

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class RetrofitDataSource @Inject constructor() {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://revolut.duckdns.org/")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val ratesApi: RatesApi = retrofit.create(RatesApi::class.java)
}