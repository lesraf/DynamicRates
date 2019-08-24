package com.rl.dynamicrates.dagger

import com.rl.dynamicrates.ui.RatesActivity
import com.rl.dynamicrates.ui.RatesAdapter
import com.rl.dynamicrates.ui.RatesAdapterPresenter
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
