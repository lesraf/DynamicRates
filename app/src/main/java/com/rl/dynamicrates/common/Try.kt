package com.rl.dynamicrates.common

sealed class Try<out A> {
    class Error(val throwable: Throwable) : Try<Nothing>()
    class Success<A>(val success: A) : Try<A>()
}