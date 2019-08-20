package com.rl.dynamicrates.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.rate_view.view.*

class RateViewHolder(root: View) : RecyclerView.ViewHolder(root) {
    fun update(rateModel: RateModel) {
        with(rateModel.currencyWithFlagModel) {
            itemView.currencyFlag.setImageResource(flagRes)
            itemView.currencyAbbreviation.text = currency.currencyCode
            itemView.currencyDisplayName.text = currency.displayName
        }
        itemView.currencyAmount.setText(rateModel.amount.toString())
    }

}
