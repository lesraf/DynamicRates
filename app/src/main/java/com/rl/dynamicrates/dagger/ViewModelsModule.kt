package com.rl.dynamicrates.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rl.dynamicrates.ui.RatesViewModel
import com.rl.dynamicrates.ui.RatesViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    @Binds
    abstract fun bindViewModelFactory(factory: RatesViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(RatesViewModel::class)
    abstract fun bindRatesViewModel(ratesViewModel: RatesViewModel): ViewModel
}