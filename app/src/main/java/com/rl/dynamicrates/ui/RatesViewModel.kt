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

    private val syncObject = Any()

    var chosenBase: RateModel = RateModel(CurrencyWithFlagModel.EUR, 100.0, true)
    var currentList: ArrayList<RateModel>? = null

    private val disposable: Disposable

    init {
        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .startWith(0)
            .observeOn(Schedulers.io())
            .flatMapSingle { getRatesUseCase.run(chosenBase.currencyCode()) }
            .observeOn(Schedulers.computation())
            .map { entities ->
                val list = ArrayList<RateModel>()

                list.add(chosenBase)
                return@map currentList?.let { currentList ->
                    populateExistingList(currentList, entities, list)
                } ?: run {
                    populateNewList(entities, list)
                }
            }
            .subscribe(
                { rateModels ->
                    currentList = rateModels
                    _ratesListLiveData.postValue(rateModels)
                },
                { throwable -> Timber.e(throwable) }
            )
    }

    private fun populateNewList(
        entities: RatesEntity,
        list: ArrayList<RateModel>
    ): ArrayList<RateModel> {
        for (currency in entities.rates.keys) {
            entities.rates[currency]?.let { rate ->
                val rateModel = RateModel(CurrencyWithFlagModel.fromString(currency), applyRate(rate))
                list.add(rateModel)
            }
        }

        return list
    }

    private fun populateExistingList(
        currentList: ArrayList<RateModel>,
        entities: RatesEntity,
        list: ArrayList<RateModel>
    ): ArrayList<RateModel> {
        for (i in 1 until currentList.size) {
            val currencyCode = currentList[i].currencyCode()
            val rate = entities.rates[currencyCode] ?: 0.0
            list.add(RateModel(CurrencyWithFlagModel.fromString(currencyCode), applyRate(rate)))
        }

        return list
    }

    fun ratesListLiveData(): LiveData<List<RateModel>> {
        return _ratesListLiveData
    }


    fun onRateClick(rateModel: RateModel) {
        synchronized(syncObject) {
            if (!isBaseCurrency(rateModel)) {
                currentList?.get(0)?.let { previousBase ->
                    currentList?.remove(previousBase)
                    currentList?.add(0, previousBase.copy(isBase = false))
                }
                currentList?.remove(rateModel)
                chosenBase = rateModel.copy(isBase = true)
                currentList?.add(0, chosenBase)
                _ratesListLiveData.postValue(ArrayList(currentList))
            }
        }
    }

    fun onAmountChange(amount: Double) {
        chosenBase = chosenBase.copy(amount = amount)

        // TODO recalculate amounts
    }

    private fun applyRate(rate: Double): Double {
        return rate * chosenBase.amount
    }

    private fun isBaseCurrency(rateModel: RateModel): Boolean {
        return chosenBase.currencyWithFlagModel == rateModel.currencyWithFlagModel
    }
}