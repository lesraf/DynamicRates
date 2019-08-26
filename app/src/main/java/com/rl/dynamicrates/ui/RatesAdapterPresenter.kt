package com.rl.dynamicrates.ui

import com.rl.dynamicrates.ui.models.RateModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class RatesAdapterPresenter @Inject constructor(
    private val calculateRatesDiffUseCase: CalculateRatesDiffUseCase
) : RatesAdapterContract.Presenter {
    private var view: RatesAdapterContract.View? = null
    private val ratesList = ArrayList<RateModel>()
    private var disposable: Disposable? = null

    override fun attachView(view: RatesAdapterContract.View) {
        this.view = view
    }

    override fun onUpdate(newRatesList: List<RateModel>) {
        disposable = calculateRatesDiffUseCase.run(ratesList, newRatesList)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { diffResult ->
                    ratesList.clear()
                    ratesList.addAll(newRatesList)
                    view?.dispatchUpdates(diffResult)
                },
                {
                    Timber.e(it)
                }
            )
    }

    override fun onGetItemCount() = ratesList.size

    override fun onBindViewHolder(position: Int) = ratesList[position]

    override fun onBindViewHolder(payloads: MutableList<Any>): RateViewHolder.PayloadChange? {
        if (payloads.isNotEmpty()) {
            return payloads.last() as? RateViewHolder.PayloadChange
        }
        return null
    }

    override fun onDestroy() {
        disposable?.dispose()
    }
}