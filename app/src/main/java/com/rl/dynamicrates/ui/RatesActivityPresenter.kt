package com.rl.dynamicrates.ui

import com.rl.dynamicrates.common.Try
import com.rl.dynamicrates.domain.GetRatesUseCase
import com.rl.dynamicrates.domain.RatesEntity
import com.rl.dynamicrates.ui.models.CurrencyWithFlagModel
import com.rl.dynamicrates.ui.models.RateModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RatesActivityPresenter @Inject constructor(
    private val getRatesUseCase: GetRatesUseCase
) : RatesActivityContract.Presenter {

    private val initialAmount = 100.0
    private val syncObject = Any()

    private var showProgressBar = true
    private var view: RatesActivityContract.View? = null
    private var intervalDisposable: Disposable? = null
    private var snackbarDisposable: Disposable? = null
    private var chosenBase: RateModel = RateModel(
        CurrencyWithFlagModel.EUR,
        initialAmount,
        true
    )
    private var currentList: ArrayList<RateModel>? = null
    private var currentRatesEntity: RatesEntity? = null

    override fun attachView(view: RatesActivityContract.View) {
        this.view = view
    }

    override fun onStart() {
        startFetchingRates()
    }

    override fun onStop() {
        intervalDisposable?.dispose()
    }

    override fun onDestroy() {
        intervalDisposable?.dispose()
        snackbarDisposable?.dispose()
    }

    override fun onRateClick(rateModel: RateModel) {
        synchronized(syncObject) {
            if (!isBaseCurrency(rateModel)) {
                currentList?.get(0)?.let { previousBase ->
                    currentList?.remove(previousBase)
                    currentList?.add(0, previousBase.copy(isBase = false))
                }
                currentList?.remove(rateModel)
                chosenBase = rateModel.copy(isBase = true)
                currentList?.add(0, chosenBase)
                view?.updateRates(ArrayList(currentList))
            }
        }
    }

    override fun onAmountChange(rateModel: RateModel) {
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
            view?.updateRates(ArrayList(currentList))
        }
    }

    private fun startFetchingRates() {
        if (showProgressBar) {
            showProgressBar = false
            view?.showProgressBar()
        }

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
            .observeOn(AndroidSchedulers.mainThread())
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
                val rateModel = RateModel(
                    CurrencyWithFlagModel.fromString(currency), applyRate(rate)
                )
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
        view?.updateRates(ArrayList(currentList))
        view?.hideProgressBar()
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
            list.add(
                RateModel(
                    CurrencyWithFlagModel.fromString(
                        currencyCode
                    ), applyRate(rate)
                )
            )
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
        view?.showErrorSnackbar()
        snackbarDisposable = Observable
            .timer(1, TimeUnit.SECONDS)
            .subscribe { view?.hideErrorSnackbar() }
    }

}