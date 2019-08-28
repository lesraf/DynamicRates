package com.rl.dynamicrates.dagger

import com.rl.dynamicrates.sources.RetrofitDataSource
import dagger.Module
import dagger.Provides

@Module
class AppModule {
    @Provides
    fun providesRatesApi(retrofitDataSource: RetrofitDataSource) = retrofitDataSource.ratesApi
}