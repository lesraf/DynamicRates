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
    @Inject
    lateinit var ratesAdapter: RatesAdapter

    private var viewModel: RatesViewModel? = null
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)

        setupViewModel()
        setupRecyclerView()
        setupLiveDatas()
    }

    override fun onStart() {
        super.onStart()
        viewModel?.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewModel?.onStop()
    }

    override fun onDestroy() {
        ratesAdapter.onDestroy()
        super.onDestroy()
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[RatesViewModel::class.java]
    }

    private fun setupRecyclerView() {
        ratesList.apply {
            layoutManager = LinearLayoutManager(this@RatesActivity)
            adapter = ratesAdapter
        }
    }

    private fun setupLiveDatas() {
        viewModel?.ratesListData()
            ?.observe(
                this,
                Observer { ratesModels -> ratesAdapter.update(ratesModels) }
            )

        viewModel?.progressBarVisibility()
            ?.observe(
                this,
                Observer { visible ->
                    if (visible) progressBar.visible() else progressBar.gone()
                }
            )

        viewModel?.errorSnackbarVisibility()
            ?.observe(this, Observer { visible ->
                if (visible) {
                    initSnackbarIfShould()
                    snackbar?.show()
                } else {
                    snackbar?.dismiss()
                }
            })
    }

    private fun initSnackbarIfShould() {
        if (snackbar == null) {
            snackbar = Snackbar.make(container, R.string.error_could_not_fetch_rates, Snackbar.LENGTH_INDEFINITE)
        }
    }

    fun prepareOnAmountChangeListener(): OnAmountChangeListener {
        return { rateModel -> viewModel?.onAmountChange(rateModel) }
    }

    fun prepareOnRateClickListener(): OnRateClickListener {
        return { rateModel -> viewModel?.onRateClick(rateModel) }
    }
}
