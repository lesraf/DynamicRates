package com.rl.dynamicrates.ui.list.viewholder

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(value = Parameterized::class)
class DecimalDigitsInputFilterTest(
    private val input: String,
    private val result: Boolean
) {

    private val decimalDigitsInputFilter = DecimalDigitsInputFilter()

    @Test
    fun testNumberValidation() {
        val matcher = decimalDigitsInputFilter.maxTwoDecimalPointsPattern.matcher(input)
        assertThat(matcher.matches(), equalTo(result))
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index} input: {0}, isValid: {1}")
        fun data(): Iterable<Array<Any>> {
            return arrayListOf(
                arrayOf("0", true),
                arrayOf("0.", true),
                arrayOf("1000000.0", true),
                arrayOf("0.01", true),
                arrayOf(".01", true),
                arrayOf("0.012", false),
                arrayOf("0,", true),
                arrayOf("1000000,0", true),
                arrayOf("0,01", true),
                arrayOf(",01", true),
                arrayOf("0,012", false)
            )
        }
    }
}