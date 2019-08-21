package com.rl.dynamicrates.ui

import com.rl.dynamicrates.R
import java.util.*

enum class CurrencyWithFlagModel(
    val flagRes: Int,
    val currency: Currency
) {
    UNKNOWN(0, Currency.getInstance("EUR")),
    EUR(R.drawable.ic_european_union, Currency.getInstance("EUR")),
    AUD(R.drawable.ic_australia, Currency.getInstance("AUD")),
    BGN(R.drawable.ic_bulgaria, Currency.getInstance("BGN")),
    BRL(R.drawable.ic_brazil, Currency.getInstance("BRL")),
    CAD(R.drawable.ic_canada, Currency.getInstance("CAD")),
    CHF(R.drawable.ic_switzerland, Currency.getInstance("CHF")),
    CNY(R.drawable.ic_china, Currency.getInstance("CNY")),
    CZK(R.drawable.ic_czech_republic, Currency.getInstance("CZK")),
    DKK(R.drawable.ic_denmark, Currency.getInstance("DKK")),
    GBP(R.drawable.ic_united_kingdom, Currency.getInstance("GBP")),
    HKD(R.drawable.ic_hong_kong, Currency.getInstance("HKD")),
    HRK(R.drawable.ic_croatia, Currency.getInstance("HRK")),
    HUF(R.drawable.ic_hungary, Currency.getInstance("HUF")),
    IDR(R.drawable.ic_indonesia, Currency.getInstance("IDR")),
    ILS(R.drawable.ic_israel, Currency.getInstance("ILS")),
    INR(R.drawable.ic_india, Currency.getInstance("INR")),
    ISK(R.drawable.ic_iceland, Currency.getInstance("ISK")),
    JPY(R.drawable.ic_japan, Currency.getInstance("JPY")),
    KRW(R.drawable.ic_south_korea, Currency.getInstance("KRW")),
    MXN(R.drawable.ic_mexico, Currency.getInstance("MXN")),
    MYR(R.drawable.ic_malaysia, Currency.getInstance("MYR")),
    NOK(R.drawable.ic_norway, Currency.getInstance("NOK")),
    NZD(R.drawable.ic_new_zealand, Currency.getInstance("NZD")),
    PHP(R.drawable.ic_philippines, Currency.getInstance("PHP")),
    PLN(R.drawable.ic_republic_of_poland, Currency.getInstance("PLN")),
    RON(R.drawable.ic_romania, Currency.getInstance("RON")),
    RUB(R.drawable.ic_russia, Currency.getInstance("RUB")),
    SEK(R.drawable.ic_sweden, Currency.getInstance("SEK")),
    SGD(R.drawable.ic_singapore, Currency.getInstance("SGD")),
    THB(R.drawable.ic_thailand, Currency.getInstance("THB")),
    TRY(R.drawable.ic_turkey, Currency.getInstance("TRY")),
    USD(R.drawable.ic_united_states_of_america, Currency.getInstance("USD")),
    ZAR(R.drawable.ic_zambia, Currency.getInstance("ZAR"));

    companion object {
        fun fromString(currency: String): CurrencyWithFlagModel {
            return try {
                valueOf(currency)
            } catch (iae: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}