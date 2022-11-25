package com.example.e2e

import android.content.Context

object TimeoutFormatter {

    private const val SECONDS_MS = 1000
    private const val MINUTES_S = 60
    private const val HOURS_S = 60 * MINUTES_S

    fun format(timeout: Long, context: Context): String {
        val sec = (timeout + SECONDS_MS - 1) / SECONDS_MS
        return when {
            sec > HOURS_S ->
                "%d:%02d:%02d".format(
                    sec / HOURS_S,
                    (sec % HOURS_S) / MINUTES_S,
                    sec % MINUTES_S
                )
            sec > MINUTES_S ->
                "%d:%02d".format(sec / MINUTES_S, sec % MINUTES_S)
            else -> "$sec ${context.getString(R.string.auth_code_seconds)}"
        }
    }
}