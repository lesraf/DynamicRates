package com.rl.dynamicrates.ui.activity

import com.rl.dynamicrates.ui.models.RateModel

interface RatesActivityContract {
    interface View {
        fun updateRates(models: List<RateModel>)
        fun showProgressBar()
        fun hideProgressBar()
        fun showErrorSnackbar()
        fun hideErrorSnackbar()
    }
    interface Presenter {
        fun attachView(view: View)
        fun onStart()
        fun onStop()
        fun onDestroy()
        fun onAmountChange(rateModel: RateModel)
        fun onRateClick(clickedRateModel: RateModel)
    }
}