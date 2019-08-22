package com.rl.dynamicrates.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rl.dynamicrates.R
import com.rl.dynamicrates.common.gone
import com.rl.dynamicrates.common.visible
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_rates.*
import javax.inject.Inject

class RatesActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: RatesViewModel
    private lateinit var ratesAdapter: RatesAdapter

    private var snackbar: Snackbar? = null

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

        viewModel.ratesListData()
            .observe(
                this,
                Observer { ratesModels -> ratesAdapter.update(ratesModels) }
            )

        viewModel.progressBarVisibility()
            .observe(
                this,
                Observer { visible ->
                    if (visible) progressBar.visible() else progressBar.gone()
                }
            )

        viewModel.errorSnackbarVisibility()
            .observe(this, Observer { visible ->
                if (visible) {
                    initSnackbarIfShould()
                    snackbar?.show()
                } else {
                    snackbar?.dismiss()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }

    private fun initSnackbarIfShould() {
        if (snackbar == null) {
            snackbar = Snackbar.make(container, R.string.error_could_not_fetch_rates, Snackbar.LENGTH_INDEFINITE)
        }
    }


    private fun prepareOnAmountChangeListener(): OnAmountChangeListener {
        return viewModel::onAmountChange
    }

    private fun prepareOnRateClickListener(): OnRateClickListener {
        return viewModel::onRateClick
    }
}
