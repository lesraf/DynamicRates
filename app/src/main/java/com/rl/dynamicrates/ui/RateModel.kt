package com.rl.dynamicrates.ui


data class RateModel(val currencyWithFlagModel: CurrencyWithFlagModel, val amount: Double, val isBase: Boolean = false) {
    fun currencyCode() = currencyWithFlagModel.currency.currencyCode
}