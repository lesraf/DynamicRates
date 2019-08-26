package com.rl.dynamicrates.domain

import com.rl.dynamicrates.common.Try
import com.rl.dynamicrates.sources.RatesApi
import io.reactivex.Single
import javax.inject.Inject

class GetRatesUseCase @Inject constructor(private val ratesApi: RatesApi) {
    fun run(base: String): Single<Try<RatesEntity>> {
        return ratesApi.getRates(base)
            .map { response ->
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        Try.Success(body.toEntity())
                    } ?: Try.Error(Throwable("Success without body returned"))
                } else {
                    Try.Error(Throwable(response.errorBody()?.string() ?: "Error without body returned"))
                }
            }
            .onErrorReturn { throwable -> Try.Error(Throwable(throwable)) }
    }
}