package com.rl.dynamicrates.ui

import com.rl.dynamicrates.ui.models.RateModel

interface RateViewHolderContract {
    interface View {
        fun populateCurrency(
            flagRes: Int,
            currencyCode: String,
            displayName: String
        )

        fun populateAmountWithSelectionSave(amount: String, newSelection: Int)
        fun populateAmountWithoutSelectionSave(amount: String)
        fun removeTextChangedWatcher()
        fun addTextChangedWatcher()
        fun setOnClickListeners()
        fun getSelectionStart(): Int
    }
    interface Presenter {
        fun attachView(view: View)
        fun update(rateModel: RateModel)
        fun updatePayload(payloadChange: RateViewHolder.PayloadChange)
        fun onRateClick()
        fun onAmoutChange(amount: String?)
    }
}
