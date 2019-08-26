package com.rl.dynamicrates.domain

import com.rl.dynamicrates.sources.RatesResponse

data class RatesEntity(
    val base: String,
    val rates: HashMap<String, Double>
)

fun RatesResponse.toEntity(): RatesEntity =
    RatesEntity(base, HashMap(rates))