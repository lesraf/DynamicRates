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

    fun update(rateModel: RateModel) {
        populateViews(rateModel)
        setListeners(rateModel)
    }

    private fun populateViews(rateModel: RateModel) {
        with(rateModel.currencyWithFlagModel) {
            itemView.currencyFlag.setImageResource(flagRes)
            itemView.currencyAbbreviation.text = currency.currencyCode
            itemView.currencyDisplayName.text = currency.displayName
        }
        itemView.currencyAmount.setText(rateModel.amount.toString())
    }

    private fun setListeners(rateModel: RateModel) {
        if (rateModel.isBase) {
            itemView.currencyAmount.addTextChangedListener(prepareTextChangedWatcher())
        }
        itemView.setOnClickListener { onRateClickListener(rateModel) }
//        itemView.currencyAmount.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) onRateClickListener(rateModel) }
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

    fun updateAmount(finalAmount: Double) {
        itemView.currencyAmount.setText(finalAmount.toString())
    }

}
