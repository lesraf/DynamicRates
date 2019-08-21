package com.rl.dynamicrates.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rl.dynamicrates.domain.GetRatesUseCase
import com.rl.dynamicrates.domain.RatesEntity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RatesViewModel @Inject constructor(private val getRatesUseCase: GetRatesUseCase) : ViewModel() {

    private val _ratesListLiveData = MutableLiveData<List<RateModel>>()

    var chosenCurrency: String = "EUR"
    var currentList: ArrayList<RateModel>? = null

    private val disposable: Disposable

    init {
        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .startWith(0)
            .observeOn(Schedulers.io())
            .flatMapSingle { getRatesUseCase.run(chosenCurrency) }
            .map { entities ->
                val list = ArrayList<RateModel>()
                for (currency in entities.rates.keys) {
                    addRateModelToList(entities, currency, list)
                }
                list
            }
            .subscribe(
                { rateModels ->
                    currentList = rateModels
                    _ratesListLiveData.postValue(rateModels)
                },
                { throwable -> Timber.e(throwable) }
            )
    }

    fun ratesListLiveData(): LiveData<List<RateModel>> {
        return _ratesListLiveData
    }


    fun onRateClick(rateModel: RateModel) {
        Timber.d("onRateClick: $rateModel")
        chosenCurrency = rateModel.currencyWithFlagModel.name
        currentList?.remove(rateModel)
        currentList?.add(0, rateModel)
        _ratesListLiveData.postValue(ArrayList(currentList))
    }

    fun onAmountChange(amount: Double) {
        Timber.d("onAmountChange: $amount")
    }

    private fun addRateModelToList(
        entities: RatesEntity,
        currency: String,
        list: ArrayList<RateModel>
    ) {
        val amount = entities.rates[currency]
        val isBaseCurrency = isBaseCurrency(currency)
        amount?.let {
            val rateModel = RateModel(CurrencyWithFlagModel.fromString(currency), amount, isBaseCurrency)
            if (isBaseCurrency) {
                list.add(0, rateModel)
            } else {
                list.add(rateModel)
            }
        }
    }

    private fun isBaseCurrency(currency: String) = chosenCurrency.toLowerCase() == currency.toLowerCase()
}