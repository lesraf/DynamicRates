package com.rl.dynamicrates.dagger

import android.content.Context
import com.rl.dynamicrates.sources.RatesApi
import com.rl.dynamicrates.sources.mock.DataFetcher
import com.rl.dynamicrates.sources.mock.MockDataFetcher
import com.rl.dynamicrates.sources.mock.MockRatesDataSource
import com.rl.dynamicrates.sources.retrofit.RetrofitRatesDataSource
import dagger.Module
import dagger.Provides

@Module
class AppModule(private val context: Context) {
    // Data from endpoint
//    @Provides
//    fun providesRatesApi(retrofitRatesDataSource: RetrofitRatesDataSource) = retrofitRatesDataSource.ratesApi


    // Mock data from assets file
    @Provides
    fun providesMockDataFetcher(): DataFetcher = MockDataFetcher(context)

    @Provides
    fun providesRatesApi(dataFetcher: DataFetcher): RatesApi = MockRatesDataSource(dataFetcher)
}