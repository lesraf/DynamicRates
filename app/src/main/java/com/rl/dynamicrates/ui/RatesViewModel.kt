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
    private val _progressBarVisibility = MutableLiveData<Boolean>().apply { postValue(true) }
    private val _errorSnackbarVisibility = MutableLiveData<Boolean>()
    private val initialAmount = 100.0
    private val syncObject = Any()

    private var intervalDisposable: Disposable? = null
    private var snackbarDisposable: Disposable? = null
    private var chosenBase: RateModel = RateModel(CurrencyWithFlagModel.EUR, initialAmount, true)
    private var currentList: ArrayList<RateModel>? = null
    private var currentRatesEntity: RatesEntity? = null

    override fun onCleared() {
        intervalDisposable?.dispose()
        snackbarDisposable?.dispose()
        super.onCleared()
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

    private fun startFetchingRates() {
        intervalDisposable = Observable.interval(1, TimeUnit.SECONDS)
            .startWith(0)
            .observeOn(Schedulers.io())
            .flatMapSingle { getRatesUseCase.run(chosenBase.currencyCode()) }
            .observeOn(Schedulers.computation())
            .filter { tryValue ->
                when (tryValue) {
                    is Try.Error -> true
                    is Try.Success -> isResponseForCurrentBase(tryValue)
                }
            }
            .map { tryValue ->
                when (tryValue) {
                    is Try.Error -> tryValue
                    is Try.Success -> Try.Success(mapResponseToRatesList(tryValue))
                }
            }
            .subscribe(
                { tryValue ->
                    when (tryValue) {
                        is Try.Error -> showErrorSnackbar(tryValue.throwable)
                        is Try.Success -> handleResponseSuccess(tryValue)
                    }
                },
                { throwable -> showErrorSnackbar(throwable) }
            )
    }

    private fun populateNewList(
        entities: RatesEntity,
        list: ArrayList<RateModel>
    ): ArrayList<RateModel> {
        val listWithoutBase = ArrayList<RateModel>()
        for (currency in entities.rates.keys) {
            entities.rates[currency]?.let { rate ->
                val rateModel = RateModel(CurrencyWithFlagModel.fromString(currency), applyRate(rate))
                listWithoutBase.add(rateModel)
            }
        }
        listWithoutBase.sortBy { rateModel -> rateModel.currencyCode() }
        list.addAll(listWithoutBase)
        return list
    }

    private fun handleResponseSuccess(tryValue: Try.Success<ArrayList<RateModel>>) {
        val rateModels = tryValue.success
        currentList = rateModels
        _ratesListData.postValue(rateModels)
        _progressBarVisibility.postValue(false)
    }

    private fun isResponseForCurrentBase(tryValue: Try.Success<RatesEntity>) =
        tryValue.success.base == chosenBase.currencyCode()

    private fun mapResponseToRatesList(tryValue: Try.Success<RatesEntity>): ArrayList<RateModel> {
        val entity = tryValue.success
        currentRatesEntity = entity
        val list = ArrayList<RateModel>()

        list.add(chosenBase)
        currentList?.let { currentList ->
            populateExistingList(currentList, entity, list)
        } ?: run {
            populateNewList(entity, list)
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

    private fun showErrorSnackbar(throwable: Throwable) {
        Timber.e(throwable)
        _errorSnackbarVisibility.postValue(true)
        snackbarDisposable = Observable
            .timer(1, TimeUnit.SECONDS)
            .subscribe { _errorSnackbarVisibility.postValue(false) }
    }
}