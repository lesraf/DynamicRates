package com.rl.dynamicrates.ui.list

import androidx.recyclerview.widget.DiffUtil
import com.rl.dynamicrates.ui.models.RateModel
import io.reactivex.Single
import javax.inject.Inject

class CalculateRatesDiffUseCase @Inject constructor() {
    fun run(
        previousList: List<RateModel>,
        newList: List<RateModel>
    ): Single<DiffUtil.DiffResult> = Single.fromCallable {
        return@fromCallable DiffUtil.calculateDiff(
            RatesDiffUtilCallback(previousList, newList)
        )
    }
}
