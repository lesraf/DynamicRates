package com.rl.dynamicrates.ui.activity

import androidx.annotation.VisibleForTesting
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

    @VisibleForTesting
    var view: RatesActivityContract.View? = null

    private val initialAmount = 100.0
    private val syncObject = Any()

    private var showProgressBar = true
    private var intervalDisposable: Disposable? = null
    private var snackbarDisposable: Disposable? = null
    private var chosenBase: RateModel = RateModel(
        CurrencyWithFlagModel.EUR,
        initialAmount,
        true
    )
    private var currentList: ArrayList<RateModel>? = null
    private var currentRatesEntity: RatesEntity? = null
    private var chosenBaseToPreviousBaseRate: Double = 1.0

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
        snackbarDisposable?.dispose()
    }

    override fun onRateClick(clickedRateModel: RateModel) {
        synchronized(syncObject) {
            if (!isBaseCurrency(clickedRateModel)) {
                prepareChosenToPreviousBaseCurrencyRate(clickedRateModel)
                recalculateRatesForNewBase(clickedRateModel)

                currentList?.let { currentList ->
                    swapBases(currentList, clickedRateModel)
                    view?.updateRates(ArrayList(currentList))
                }
            }
        }
    }

    override fun onAmountChange(rateModel: RateModel) {
        synchronized(syncObject) {
            if (rateModel.currencyWithFlagModel == chosenBase.currencyWithFlagModel) {
                chosenBase = chosenBase.copy(amount = rateModel.amount)

                currentList = recalculateAmounts()
                view?.updateRates(ArrayList(currentList))
            }
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
            .filter(this::filterRatesResponse)
            .map(this::mapRatesResponse)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::handleRatesResponse,
                this::showErrorSnackbar
            )
    }

    private fun filterRatesResponse(tryValue: Try<RatesEntity>): Boolean {
        return when (tryValue) {
            is Try.Error -> true
            is Try.Success -> isResponseForCurrentBase(tryValue)
        }
    }

    private fun isResponseForCurrentBase(tryValue: Try.Success<RatesEntity>) =
        tryValue.success.base == chosenBase.currencyCode()

    private fun mapRatesResponse(tryValue: Try<RatesEntity>): Try<ArrayList<RateModel>> {
        return when (tryValue) {
            is Try.Error -> tryValue
            is Try.Success -> Try.Success(mapResponseToRatesList(tryValue))
        }
    }

    private fun mapResponseToRatesList(tryValue: Try.Success<RatesEntity>): ArrayList<RateModel> {
        synchronized(syncObject) {
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
    }

    private fun populateExistingList(
        currentList: ArrayList<RateModel>,
        entities: RatesEntity,
        list: ArrayList<RateModel>
    ): ArrayList<RateModel> {
        for (i in 1 until currentList.size) {
            val currencyCode = currentList[i].currencyCode()
            val rate = entities.rates[currencyCode] ?: chosenBaseToPreviousBaseRate
            list.add(
                RateModel(
                    CurrencyWithFlagModel.fromString(
                        currencyCode
                    ),
                    applyRate(rate)
                )
            )
        }
        return list
    }

    private fun applyRate(rate: Double): Double {
        return rate * chosenBase.amount
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

    private fun handleRatesResponse(tryValue: Try<ArrayList<RateModel>>?) {
        when (tryValue) {
            is Try.Error -> showErrorSnackbar(tryValue.throwable)
            is Try.Success -> handleResponseSuccess(tryValue)
        }
    }

    private fun handleResponseSuccess(tryValue: Try.Success<ArrayList<RateModel>>) {
        synchronized(syncObject) {
            val rateModels = tryValue.success
            currentList = rateModels
            view?.updateRates(ArrayList(currentList))
            view?.hideProgressBar()
        }
    }

    private fun isBaseCurrency(rateModel: RateModel): Boolean {
        return chosenBase.currencyWithFlagModel == rateModel.currencyWithFlagModel
    }

    private fun recalculateRatesForNewBase(clickedRateModel: RateModel) {
        currentRatesEntity?.let { currentRatesEntity ->
            val clickedModelRate = currentRatesEntity.rates[clickedRateModel.currencyCode()]
            clickedModelRate?.let { clickedRate ->
                val newBase = clickedRateModel.currencyCode()
                val ratesHashMap =
                    recalculateRates(clickedRate, currentRatesEntity, clickedRateModel)
                this.currentRatesEntity = RatesEntity(newBase, ratesHashMap)
            }
        }
    }

    private fun recalculateRates(
        clickedRate: Double,
        currentRatesEntity: RatesEntity,
        clickedRateModel: RateModel
    ): HashMap<String, Double> {
        val ratesHashMap = HashMap<String, Double>()
        val swapRate = 1 / clickedRate
        ratesHashMap[chosenBase.currencyCode()] = swapRate
        for (rate in currentRatesEntity.rates) {
            if (rate.key != clickedRateModel.currencyCode()) {
                ratesHashMap[rate.key] = rate.value * swapRate
            }
        }
        return ratesHashMap
    }

    private fun swapBases(
        currentList: ArrayList<RateModel>,
        clickedRateModel: RateModel
    ) {
        currentList[0].let { previousBase ->
            currentList.remove(previousBase)
            currentList.add(0, previousBase.copy(isBase = false))
        }
        for (model in currentList) {
            if (model.currencyWithFlagModel == clickedRateModel.currencyWithFlagModel) {
                currentList.remove(model)
                chosenBase = clickedRateModel.copy(isBase = true)
                break
            }
        }
        currentList.add(0, chosenBase)
    }

    private fun prepareChosenToPreviousBaseCurrencyRate(clickedRateModel: RateModel) {
        currentRatesEntity?.rates?.get(clickedRateModel.currencyCode())?.let { clickedRate ->
            chosenBaseToPreviousBaseRate = 1 / clickedRate
        } ?: run {
            chosenBaseToPreviousBaseRate = 1 / chosenBaseToPreviousBaseRate
        }
    }

    private fun recalculateAmounts(): ArrayList<RateModel> {
        val list = ArrayList<RateModel>()

        list.add(chosenBase)
        currentRatesEntity?.let { ratesEntity ->
            currentList?.let { currentModels ->
                populateExistingList(currentModels, ratesEntity, list)
            }
        }
        return list
    }


    private fun showErrorSnackbar(throwable: Throwable) {
        Timber.e(throwable)
        view?.showErrorSnackbar()
        snackbarDisposable = Observable
            .timer(1, TimeUnit.SECONDS)
            .subscribe { view?.hideErrorSnackbar() }
    }
}