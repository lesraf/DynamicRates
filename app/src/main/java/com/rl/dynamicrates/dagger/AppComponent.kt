package com.rl.dynamicrates.dagger

import com.rl.dynamicrates.DynamicRatesApplication
import dagger.Component
import dagger.android.AndroidInjectionModule

@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        ActivitiesBuilder::class
    ]
)
interface AppComponent {
    fun inject(dynamicRatesApplication: DynamicRatesApplication)
}