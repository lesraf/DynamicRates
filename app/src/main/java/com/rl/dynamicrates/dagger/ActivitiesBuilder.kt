package com.rl.dynamicrates.dagger

import com.rl.dynamicrates.ui.activity.RatesActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesBuilder {
    @ContributesAndroidInjector(modules = [RatesActivityModule::class])
    abstract fun provideRatesActivity(): RatesActivity
}