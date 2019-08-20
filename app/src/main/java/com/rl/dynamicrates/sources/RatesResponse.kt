package com.rl.dynamicrates.sources

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RatesResponse(val base: String, val rates: Map<String, Double>)