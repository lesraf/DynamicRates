package com.rl.dynamicrates.domain

import com.rl.dynamicrates.sources.RatesApi
import com.rl.dynamicrates.sources.RatesResponse
import io.reactivex.Single
import javax.inject.Inject

class GetRatesUseCase @Inject constructor(val ratesApi: RatesApi) {
    fun run(base: String): Single<RatesEntity> {
        return ratesApi.getRates(base)
            .map(RatesResponse::toEntity)
    }
}