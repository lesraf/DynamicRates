package com.rl.dynamicrates.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rl.dynamicrates.R
import com.rl.dynamicrates.common.gone
import com.rl.dynamicrates.common.visible
import com.rl.dynamicrates.ui.models.RateModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_rates.*
import javax.inject.Inject

class RatesActivity : AppCompatActivity(), RatesActivityContract.View {

    @Inject
    lateinit var presenter: RatesActivityPresenter
    @Inject
    lateinit var ratesAdapter: RatesAdapter

    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)

        presenter.attachView(this)
        setupRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        ratesAdapter.onDestroy()
        super.onDestroy()
    }

    override fun updateRates(models: List<RateModel>) {
        ratesAdapter.update(models)
    }

    override fun showProgressBar() {
        progressBar.visible()
    }

    override fun hideProgressBar() {
        progressBar.gone()
    }

    override fun showErrorSnackbar() {
        initSnackbarIfShould()
        snackbar?.show()
    }

    override fun hideErrorSnackbar() {
        snackbar?.dismiss()
    }

    private fun setupRecyclerView() {
        ratesList.apply {
            layoutManager = LinearLayoutManager(this@RatesActivity)
            adapter = ratesAdapter
        }
    }

    private fun initSnackbarIfShould() {
        if (snackbar == null) {
            snackbar = Snackbar.make(
                container,
                R.string.error_could_not_fetch_rates,
                Snackbar.LENGTH_INDEFINITE
            )
        }
    }

    fun prepareOnAmountChangeListener(): OnAmountChangeListener {
        return { rateModel -> presenter.onAmountChange(rateModel) }
    }

    fun prepareOnRateClickListener(): OnRateClickListener {
        return { rateModel -> presenter.onRateClick(rateModel) }
    }
}
