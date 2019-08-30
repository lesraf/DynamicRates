package com.rl.dynamicrates.ui.list.viewholder

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import java.util.regex.Pattern

class DecimalDigitsInputFilter : InputFilter {

    @VisibleForTesting
    val maxTwoDecimalPointsPattern = Pattern.compile("[0-9]*+((([.,])[0-9]?[0-9]?)?)|[.,]?")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val previousPartStart = dest.subSequence(0, dstart).toString()
        val previousPartEnd = dest.subSequence(dend, dest.length).toString()
        val replacement = source.subSequence(start, end).toString()
        val newText = previousPartStart + replacement + previousPartEnd
        val newTextMatcher = maxTwoDecimalPointsPattern.matcher(newText)

        return when {
            newTextMatcher.matches() -> null
            TextUtils.isEmpty(source) -> dest.subSequence(dstart, dend)
            else -> ""
        }
    }
}