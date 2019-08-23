package com.rl.dynamicrates.common

import android.text.Editable
import android.text.TextWatcher

abstract class TextChangedWatcher : TextWatcher {
    abstract override fun afterTextChanged(editable: Editable?)

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
        //no-op
    }

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        //no-op
    }
}