package com.rl.dynamicrates.ui.list.viewholder

import androidx.annotation.VisibleForTesting
import com.rl.dynamicrates.ui.models.RateModel
import java.text.DecimalFormat

class RateViewHolderPresenter(
    val onRateClickListener: OnRateClickListener,
    val onAmountChangeListener: OnAmountChangeListener
) : RateViewHolderContract.Presenter {

    private lateinit var rateModel: RateModel
    private var isTextWatcherAdded = false

    @VisibleForTesting
    var view: RateViewHolderContract.View? = null
    private var amountFormat = DecimalFormat("#.##")

    override fun attachView(view: RateViewHolderContract.View) {
        this.view = view
    }

    override fun update(rateModel: RateModel) {
        this.rateModel = rateModel
        with(rateModel.currencyWithFlagModel) {
            view?.populateCurrency(
                flagRes,
                currency.currencyCode,
                currency.displayName
            )
        }

        populateAmount()
        updateTextWatcherListener()
        view?.setOnClickListeners()
    }

    override fun updatePayload(payloadChange: RateViewHolder.PayloadChange) {
        rateModel = rateModel.copy(amount = payloadChange.amount, isBase = payloadChange.isBase)
        populateAmount()
        updateTextWatcherListener()
    }

    override fun onRateClick() {
        onRateClickListener(rateModel)
    }

    override fun onAmoutChange(amount: String?) {
        rateModel = rateModel.copy(amount = amount?.toDoubleOrNull() ?: 0.0)
        onAmountChangeListener(rateModel)
    }

    private fun updateTextWatcherListener() {
        if (rateModel.isBase) {
            if (!isTextWatcherAdded) {
                isTextWatcherAdded = true
                view?.addTextChangedWatcher()
            }
        } else {
            if (isTextWatcherAdded) {
                isTextWatcherAdded = false
                view?.removeTextChangedWatcher()
            }
        }
    }

    private fun populateAmount() {
        val amount = formatAmount(rateModel.amount)
        if (rateModel.isBase) {
            populateAmountWithSelectionSave(amount)
        } else {
            view?.populateAmountWithoutSelectionSave(amount)
        }
    }

    private fun formatAmount(amount: Double): String {
        return if (amount == 0.0) {
            ""
        } else {
            amountFormat.format(amount)
        }
    }

    private fun populateAmountWithSelectionSave(amount: String) {
        view?.let { view ->
            val selectionStart = view.getSelectionStart()
            val newSelection =
                if (amount.length < selectionStart) amount.length else selectionStart
            view.populateAmountWithSelectionSave(amount, newSelection)
        }
    }
}
