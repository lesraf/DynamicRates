package com.rl.dynamicrates.dagger

import com.rl.dynamicrates.ui.activity.RatesActivity
import com.rl.dynamicrates.ui.list.RatesAdapter
import com.rl.dynamicrates.ui.list.RatesAdapterPresenter
import dagger.Module
import dagger.Provides

@Module
class RatesActivityModule {
    @Provides
    fun provideRatesAdapter(
        ratesActivity: RatesActivity,
        ratesAdapterPresenter: RatesAdapterPresenter
    ): RatesAdapter {
        return RatesAdapter(
            ratesActivity.prepareOnRateClickListener(),
            ratesActivity.prepareOnAmountChangeListener(),
            ratesAdapterPresenter
        )
    }
}
