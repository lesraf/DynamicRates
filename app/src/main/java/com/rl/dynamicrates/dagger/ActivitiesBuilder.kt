package com.rl.dynamicrates.dagger

import com.rl.dynamicrates.ui.RatesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesBuilder {
    @ContributesAndroidInjector()
    abstract fun provideRatesActivity(): RatesActivity
}