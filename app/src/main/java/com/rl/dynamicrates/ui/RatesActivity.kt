package com.rl.dynamicrates.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.rl.dynamicrates.R
import com.rl.dynamicrates.domain.GetRatesUseCase
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_rates.*
import javax.inject.Inject

class RatesActivity : AppCompatActivity() {

    @Inject
    lateinit var useCase: GetRatesUseCase

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: RatesViewModel

    private lateinit var ratesAdapter: RatesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[RatesViewModel::class.java]

        ratesAdapter = RatesAdapter(prepareOnRateClickListener(), prepareOnAmountChangeListener())

        ratesList.apply {
            layoutManager = LinearLayoutManager(this@RatesActivity)
            adapter = ratesAdapter
        }

        viewModel.ratesListLiveData()
            .observe(
                this,
                Observer { ratesModels -> ratesAdapter.update(ratesModels) }
            )
    }

    private fun prepareOnAmountChangeListener(): OnAmountChangeListener {
        return viewModel::onAmountChange
    }

    private fun prepareOnRateClickListener(): OnRateClickListener {
        return viewModel::onRateClick
    }
}
