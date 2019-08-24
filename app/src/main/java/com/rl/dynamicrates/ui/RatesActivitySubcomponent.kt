package com.rl.dynamicrates.ui

import com.rl.dynamicrates.dagger.RatesActivityModule
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [RatesActivityModule::class])
interface RatesActivitySubcomponent : AndroidInjector<RatesActivity> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<RatesActivity>
}