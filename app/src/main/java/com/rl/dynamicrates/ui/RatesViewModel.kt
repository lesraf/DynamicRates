package com.rl.dynamicrates.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rl.dynamicrates.common.Try
import com.rl.dynamicrates.domain.GetRatesUseCase
import com.rl.dynamicrates.domain.RatesEntity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RatesViewModel @Inject constructor(private val getRatesUseCase: GetRatesUseCase) : ViewModel() {

    private val _ratesListData = MutableLiveData<List<RateModel>>()

    private val _progressBarVisibility = MutableLiveData<Boolean>()

    private val _errorSnackbarVisibility = MutableLiveData<Boolean>()

    private val syncObject = Any()

    var chosenBase: RateModel = RateModel(CurrencyWithFlagModel.EUR, 100.0, true)
    var currentList: ArrayList<RateModel>? = null
    var currentRatesEntity: RatesEntity? = null

    private var intervalDisposable: Disposable? = null
    private var snackbarDisposable: Disposable? = null

    init {
        _progressBarVisibility.postValue(true)
    }

    fun ratesListData(): LiveData<List<RateModel>> {
        return _ratesListData
    }

    fun progressBarVisibility(): LiveData<Boolean> {
        return _progressBarVisibility
    }

    fun errorSnackbarVisibility(): LiveData<Boolean> {
        return _errorSnackbarVisibility
    }

    fun onStart() {
        startFetchingRates()
    }

    fun onStop() {
        intervalDisposable?.dispose()
    }

    private fun startFetchingRates() {
        intervalDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .startWith(0)
            .observeOn(Schedulers.io())
            .flatMapSingle { getRatesUseCase.run(chosenBase.currencyCode()) }
            .observeOn(Schedulers.computation())
            .filter { tryValue ->
                when (tryValue) {
                    is Try.Error -> true
                    is Try.Success -> tryValue.success.base == chosenBase.currencyCode()
                }
            }
            .map { tryValue ->
                when (tryValue) {
                    is Try.Error -> tryValue
                    is Try.Success -> {
                        val entity = tryValue.success
                        currentRatesEntity = entity
                        val list = ArrayList<RateModel>()

                        list.add(chosenBase)
                        currentList?.let { currentList ->
                            populateExistingList(currentList, entity, list)
                        } ?: run {
                            populateNewList(entity, list)
                        }
                        return@map Try.Success(list)
                    }
                }

            }
            .subscribe(
                { tryValue ->
                    when (tryValue) {
                        is Try.Error -> {
                            Timber.e(tryValue.throwable)
                            showErrorSnackbar()
                        }
                        is Try.Success -> {
                            val rateModels = tryValue.success
                            currentList = rateModels
                            _ratesListData.postValue(rateModels)
                            _progressBarVisibility.postValue(false)
                        }
                    }
                },
                { throwable -> Timber.e(throwable) }
            )
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
                _ratesListData.postValue(ArrayList(currentList))
            }
        }
    }

    fun onAmountChange(rateModel: RateModel) {
        if (rateModel.currencyWithFlagModel == chosenBase.currencyWithFlagModel) {
            chosenBase = chosenBase.copy(amount = rateModel.amount)

            val list = ArrayList<RateModel>()

            list.add(chosenBase)
            currentRatesEntity?.let { ratesEntity ->
                currentList?.let { currentModels ->
                    populateExistingList(currentModels, ratesEntity, list)
                }
            }
            currentList = list
            _ratesListData.postValue(currentList)
        }
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

    private fun applyRate(rate: Double): Double {
        return rate * chosenBase.amount
    }

    private fun isBaseCurrency(rateModel: RateModel): Boolean {
        return chosenBase.currencyWithFlagModel == rateModel.currencyWithFlagModel
    }

    private fun showErrorSnackbar() {
        _errorSnackbarVisibility.postValue(true)
        snackbarDisposable = Observable
            .timer(1, TimeUnit.SECONDS)
            .subscribe { _errorSnackbarVisibility.postValue(false) }
    }

    override fun onCleared() {
        intervalDisposable?.dispose()
        snackbarDisposable?.dispose()
        super.onCleared()
    }
}