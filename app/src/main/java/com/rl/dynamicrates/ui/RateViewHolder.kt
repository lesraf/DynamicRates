package com.rl.dynamicrates.ui

import android.text.Editable
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rl.dynamicrates.common.TextChangedWatcher
import com.rl.dynamicrates.ui.models.RateModel
import kotlinx.android.synthetic.main.rate_view.view.*

typealias OnRateClickListener = (RateModel) -> Unit
typealias OnAmountChangeListener = (RateModel) -> Unit

class RateViewHolder(
    root: View,
    val presenter: RateViewHolderContract.Presenter
) : RecyclerView.ViewHolder(root), RateViewHolderContract.View {

    private var textChangedWatcher: TextChangedWatcher? = null

    init {
        presenter.attachView(this)
    }

    override fun populateCurrency(
        flagRes: Int,
        currencyCode: String,
        displayName: String
    ) {
        itemView.currencyFlag.setImageResource(flagRes)
        itemView.currencyAbbreviation.text = currencyCode
        itemView.currencyDisplayName.text = displayName
    }

    override fun populateAmountWithSelectionSave(amount: String, newSelection: Int) {
        setAmount(amount)
        itemView.currencyAmount.setSelection(newSelection)
    }

    override fun populateAmountWithoutSelectionSave(amount: String) {
        setAmount(amount)
    }

    override fun getSelectionStart() = itemView.currencyAmount.selectionStart

    fun update(rateModel: RateModel) {
        presenter.update(rateModel)
    }

    fun updatePayload(payloadChange: PayloadChange) {
        presenter.updatePayload(payloadChange)
    }

    private fun setAmount(amount: String) {
        itemView.currencyAmount.setText(amount)
    }

    override fun setOnClickListeners() {
        itemView.setOnClickListener {
            itemView.currencyAmount.requestFocus()
            presenter.onRateClick()
        }
        itemView.currencyAmount.setOnTouchListener { _, event ->
            if (MotionEvent.ACTION_UP == event.action) {
                presenter.onRateClick()
            }
            false
        }
    }

    override fun addTextChangedWatcher() {
        textChangedWatcher = prepareTextChangedWatcher()
        itemView.currencyAmount.addTextChangedListener(textChangedWatcher)
    }

    override fun removeTextChangedWatcher() {
        textChangedWatcher?.let {
            itemView.currencyAmount.removeTextChangedListener(textChangedWatcher)
        }
    }

    private fun prepareTextChangedWatcher(): TextChangedWatcher {
        return object : TextChangedWatcher() {
            override fun afterTextChanged(editable: Editable?) {
                editable?.let { changedText ->
                    presenter.onAmoutChange(changedText.toString())
                }
            }
        }
    }

    data class PayloadChange(val amount: Double, val isBase: Boolean)
}
