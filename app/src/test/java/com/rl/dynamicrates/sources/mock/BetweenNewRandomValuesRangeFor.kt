package com.rl.dynamicrates.sources.mock

import org.hamcrest.CustomTypeSafeMatcher

class BetweenNewRandomValuesRangeFor(private val itemToCompareTo: Double?) :
    CustomTypeSafeMatcher<Double>(
        "Number should be at least " +
                "${calculateMinValueInclusive(itemToCompareTo)} " +
                "or smaller than " +
                "${calculateMaxValueExclusive(itemToCompareTo)}"
    ) {
    override fun matchesSafely(item: Double?): Boolean {
        requireNotNull(item)

        val minValue = calculateMinValueInclusive(itemToCompareTo)
        val maxValue = calculateMaxValueExclusive(itemToCompareTo)

        requireNotNull(minValue)
        requireNotNull(maxValue)

        return item >= minValue && item < maxValue
    }
}

private fun calculateMinValueInclusive(itemToCompareTo: Double?) =
    itemToCompareTo?.times(0.98)

private fun calculateMaxValueExclusive(itemToCompareTo: Double?) =
    itemToCompareTo?.times(1.02)