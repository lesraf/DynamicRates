package com.rl.dynamicrates.ui

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface RatesActivitySubcomponent : AndroidInjector<RatesActivity> {
    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<RatesActivity> {}
}