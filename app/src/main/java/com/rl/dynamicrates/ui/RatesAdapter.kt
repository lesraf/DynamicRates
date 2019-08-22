package com.rl.dynamicrates.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rl.dynamicrates.R

class RatesAdapter(
    private val onClickListener: OnRateClickListener,
    private val onAmountChangeListener: OnAmountChangeListener
) : RecyclerView.Adapter<RateViewHolder>() {

    private val ratesList = mutableListOf<RateModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rate_view, parent, false)
        return RateViewHolder(view, onClickListener, onAmountChangeListener)
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        holder.update(ratesList[position])
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val change = payloads.last() as? RateViewHolder.PayloadChange
            if (change != null) {
                holder.updatePayload(change)
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }
    }

    override fun getItemCount() = ratesList.size

    fun update(newRatesList: List<RateModel>) {
        val diffResult = DiffUtil.calculateDiff(
            RatesDiffUtilCallback(
                ratesList,
                newRatesList
            )
        )
        ratesList.clear()
        ratesList.addAll(newRatesList)
        diffResult.dispatchUpdatesTo(this)
    }
}
