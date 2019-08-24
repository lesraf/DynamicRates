package com.rl.dynamicrates.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.rl.dynamicrates.R
import com.rl.dynamicrates.ui.models.RateModel
import javax.inject.Inject

class RatesAdapter @Inject constructor(
    private val onClickListener: OnRateClickListener,
    private val onAmountChangeListener: OnAmountChangeListener,
    private val presenter: RatesAdapterPresenter
) : RecyclerView.Adapter<RateViewHolder>(), RatesAdapterContract.View {

    init {
        presenter.attachView(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rate_view, parent, false)
        return RateViewHolder(view, onClickListener, onAmountChangeListener)
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        holder.update(presenter.onBindViewHolder(position))
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int, payloads: MutableList<Any>) {
        presenter.onBindViewHolder(payloads)?.let { change ->
            holder.updatePayload(change)
        } ?: super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount() = presenter.onGetItemCount()

    fun update(newRatesList: List<RateModel>) {
        presenter.onUpdate(newRatesList)
    }

    override fun dispatchUpdates(result: DiffUtil.DiffResult) {
        result.dispatchUpdatesTo(this)
    }
}
