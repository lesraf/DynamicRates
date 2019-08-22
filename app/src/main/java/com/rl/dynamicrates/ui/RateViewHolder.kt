package com.rl.dynamicrates.ui

import android.text.Editable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rate_view.view.*
import java.text.DecimalFormat

typealias OnRateClickListener = (RateModel) -> Unit
typealias OnAmountChangeListener = (RateModel) -> Unit

class RateViewHolder(
    root: View,
    val onRateClickListener: OnRateClickListener,
    val onAmountChangeListener: OnAmountChangeListener
) : RecyclerView.ViewHolder(root) {

    var textChangedWatcher: TextChangedWatcher? = null
    var amountFormat = DecimalFormat("#.##")

    lateinit var rateModel: RateModel

    fun update(rateModel: RateModel) {
        this.rateModel = rateModel
        populateViews()
        setListeners()
    }

    fun updatePayload(payloadChange: PayloadChange) {
        populateAmount(payloadChange.amount)
        updateTextChangedWatcher(payloadChange.isBase)
        rateModel = rateModel.copy(amount = payloadChange.amount, isBase = payloadChange.isBase)
    }

    private fun populateViews() {
        with(rateModel.currencyWithFlagModel) {
            itemView.currencyFlag.setImageResource(flagRes)
            itemView.currencyAbbreviation.text = currency.currencyCode
            itemView.currencyDisplayName.text = currency.displayName
        }

        populateAmount(rateModel.amount)
    }

    private fun populateAmount(amount: Double) {
        val amountText = if (amount == 0.0) {
            ""
        } else {
            amountFormat.format(amount)
        }
        if (rateModel.isBase) {
            val selectionStart = itemView.currencyAmount.selectionStart
            setAmount(amountText)
            val newSelection = if (amountText.length < selectionStart) amountText.length else selectionStart
            itemView.currencyAmount.setSelection(newSelection)
        } else {
            setAmount(amountText)
        }
    }

    private fun setAmount(amount: String) {
        itemView.currencyAmount.setText(amount)
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
                    rateModel = rateModel.copy(amount = changedText.toString().toDoubleOrNull() ?: 0.0)
                    onAmountChangeListener(rateModel)
                }
            }
        }
    }

    data class PayloadChange(val amount: Double, val isBase: Boolean)
}
