package com.rl.dynamicrates.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rl.dynamicrates.R
import com.rl.dynamicrates.domain.GetRatesUseCase
import com.rl.dynamicrates.sources.RetrofitDataSource
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_rates.*
import javax.inject.Inject

class RatesActivity : AppCompatActivity() {

    @Inject
    lateinit var useCase: GetRatesUseCase

    private val viewAdapter = RatesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)

        ratesList.apply {
            layoutManager = LinearLayoutManager(this@RatesActivity)
            adapter = viewAdapter
        }

        viewAdapter.update(
            listOf(
                RateModel(CurrencyWithFlagModel.USD, 1183.06),
                RateModel(CurrencyWithFlagModel.EUR, 3204.10),
                RateModel(CurrencyWithFlagModel.SEK, 2154.49)
            )
        )

        // testing purposes only
        useCase.ratesApi.getRates("EUR")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { success -> Log.d("RatesActivity", "success: $success"); }
    }
}
