package com.rl.dynamicrates.domain

import com.rl.dynamicrates.sources.RatesResponse
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class RatesEntityTest {
    @Test
    fun `return entity with base and populated map for map with entries`() {
        // given
        val base = "AUD"
        val eurRate = "EUR" to 4.21
        val usdRate = "USE" to 3.12
        val rates = mapOf(eurRate, usdRate)

        // when
        val entity = RatesResponse(base, rates).toEntity()

        // then
        assertThat(entity.base, equalTo(base))
        entity.rates.forEach { actualRate ->
            assertThat(
                actualRate.value,
                equalTo(rates[actualRate.key])
            )
        }
    }

    @Test
    fun `return entity with base and empty map for empty map`() {
        // given
        val base = "AUD"
        val rates = HashMap<String, Double>()

        // when
        val entity = RatesResponse(base, rates).toEntity()

        // then
        assertThat(entity.base, equalTo(base))
        assertThat(entity.rates.size, equalTo(0))
    }
}