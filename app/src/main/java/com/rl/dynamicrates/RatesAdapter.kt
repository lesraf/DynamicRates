package com.rl.dynamicrates

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RatesAdapter : RecyclerView.Adapter<RateViewHolder>() {

    private val ratesList = mutableListOf<RateModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.rate_view, parent, false)
        return RateViewHolder(view)
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        holder.update(ratesList[position])
    }

    override fun getItemCount() = ratesList.size

    fun update(events: List<RateModel>) {
//        val diffResult = DiffUtil.calculateDiff(
//                EventsDiffUtilCallback(
//                        eventsList,
//                        events
//                )
//        )
        ratesList.clear()
        ratesList.addAll(events)
//        diffResult.dispatchUpdatesTo(this)
    }
}
