package com.rl.dynamicrates.ui.list

import androidx.recyclerview.widget.DiffUtil
import com.rl.dynamicrates.ui.list.viewholder.RateViewHolder
import com.rl.dynamicrates.ui.models.RateModel

interface RatesAdapterContract {
    interface View {
        fun dispatchUpdates(result: DiffUtil.DiffResult)
    }

    interface Presenter {
        fun attachView(view: View)
        fun onUpdate(newRatesList: List<RateModel>)
        fun onGetItemCount(): Int
        fun onBindViewHolder(position: Int): RateModel
        fun onBindViewHolder(payloads: MutableList<Any>): RateViewHolder.PayloadChange?
        fun onDestroy()
    }
}