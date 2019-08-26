package com.rl.dynamicrates.ui.models


data class RateModel(
    val currencyWithFlagModel: CurrencyWithFlagModel,
    val amount: Double,
    val isBase: Boolean = false
) {
    fun currencyCode(): String = currencyWithFlagModel.currency.currencyCode
}