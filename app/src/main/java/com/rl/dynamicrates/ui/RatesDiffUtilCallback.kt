package com.rl.dynamicrates.ui

import androidx.recyclerview.widget.DiffUtil

class RatesDiffUtilCallback(
    private val oldList: List<RateModel>,
    private val newList: List<RateModel>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].currencyWithFlagModel == newList[newItemPosition].currencyWithFlagModel &&
                oldList[oldItemPosition].isBase == newList[newItemPosition].isBase
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].currencyWithFlagModel == newList[newItemPosition].currencyWithFlagModel &&
                oldList[oldItemPosition].isBase == newList[newItemPosition].isBase &&
                oldList[oldItemPosition].amount == newList[newItemPosition].amount
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return newList[newItemPosition].amount
    }
}