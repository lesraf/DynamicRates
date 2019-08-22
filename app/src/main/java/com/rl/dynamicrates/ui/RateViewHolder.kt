package com.rl.dynamicrates.ui

import android.text.Editable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rate_view.view.*

typealias OnRateClickListener = (RateModel) -> Unit
typealias OnAmountChangeListener = (Double) -> Unit

class RateViewHolder(
    root: View,
    val onRateClickListener: OnRateClickListener,
    val onAmountChangeListener: OnAmountChangeListener
) : RecyclerView.ViewHolder(root) {

    var textChangedWatcher: TextChangedWatcher? = null

    lateinit var rateModel: RateModel

    fun update(rateModel: RateModel) {
        this.rateModel = rateModel
        populateViews()
        setListeners()
    }

    fun updatePayload(payloadChange: PayloadChange) {
        itemView.currencyAmount.setText(payloadChange.amount.toString())
        updateTextChangedWatcher(payloadChange.isBase)
        rateModel = rateModel.copy(amount = payloadChange.amount, isBase = payloadChange.isBase)
    }

    private fun populateViews() {
        with(rateModel.currencyWithFlagModel) {
            itemView.currencyFlag.setImageResource(flagRes)
            itemView.currencyAbbreviation.text = currency.currencyCode
            itemView.currencyDisplayName.text = currency.displayName
        }
        itemView.currencyAmount.setText(rateModel.amount.toString())
    }

    private fun setListeners() {
        updateTextChangedWatcher(rateModel.isBase)
        itemView.setOnClickListener {
            itemView.currencyAmount.requestFocus()
            onRateClickListener(rateModel)
        }
        itemView.currencyAmount.setOnClickListener { onRateClickListener(rateModel) }
    }

    private fun updateTextChangedWatcher(isBase: Boolean) {
        if (isBase) {
            textChangedWatcher = prepareTextChangedWatcher()
            itemView.currencyAmount.addTextChangedListener(textChangedWatcher)
        } else {
            textChangedWatcher?.let { itemView.currencyAmount.removeTextChangedListener(textChangedWatcher) }
        }
    }

    private fun prepareTextChangedWatcher(): TextChangedWatcher {
        return object : TextChangedWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                editable?.let { changedText ->
                    onAmountChangeListener(changedText.toString().toDoubleOrNull() ?: 0.0)
                }
            }
        }
    }

    data class PayloadChange(val amount: Double, val isBase: Boolean)
}
