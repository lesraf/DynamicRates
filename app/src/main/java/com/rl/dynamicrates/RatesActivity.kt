package com.rl.dynamicrates

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_rates.*

class RatesActivity : AppCompatActivity() {

    private val viewAdapter = RatesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rates)

        ratesList.apply {
            layoutManager = LinearLayoutManager(this@RatesActivity)
            adapter = viewAdapter
        }

        viewAdapter.update(
            listOf(
                RateModel(CurrencyWithFlag.USD, 1183.06),
                RateModel(CurrencyWithFlag.EUR, 3204.10),
                RateModel(CurrencyWithFlag.SEK, 2154.49)
            )
        )
    }
}
