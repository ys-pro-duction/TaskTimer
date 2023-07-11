package com.ys_production.tasktimer

import android.util.Log
import java.util.Date

private const val TAG = "Timing"

class Timing(val taskId: Int, val startTime: Long = Date().time / 1000, var id: Long = 0) {
    var duration: Long = 0
        private set
    fun setDuration() {
        duration = Date().time / 1000 - startTime
        Log.d(TAG, "setDuration: startTime = $startTime, duration = $duration")
    }
}