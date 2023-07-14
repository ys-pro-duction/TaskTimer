package com.ys_production.tasktimer.debug

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import com.ys_production.tasktimer.TasksContract
import com.ys_production.tasktimer.TimingsContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import kotlin.math.roundToInt

internal class TestTiming internal constructor(var taskId: Long, date: Long, var duration: Long) {
    var startTime: Long = 0

    init {
        this.startTime = date / 1000
    }
}

object TaskData {
    private const val SEC_IN_DAY = 86400
    private const val LOWER_BOUND = 100
    private const val UPPER_BOUND = 500
    private const val MAX_DURATION = SEC_IN_DAY / 6

    @SuppressLint("Range")
    fun generateRandomTestData(contentResolver: ContentResolver) {
        val projection = arrayOf(TasksContract.Columns.ID)
        val uri = TasksContract.CONTENT_URI
        val cursor = contentResolver.query(uri, projection, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val taskId = cursor.getLong(cursor.getColumnIndex(TasksContract.Columns.ID))
                val loopCount = LOWER_BOUND + getRandomInt(UPPER_BOUND - LOWER_BOUND)
                for (i in 0 until loopCount) {
                    val randomeDate = randomDateTime()
                    val duration = getRandomInt(MAX_DURATION).toLong()
                    val testTiming = TestTiming(taskId, randomeDate, duration)
                    saveCurrentTiming(contentResolver, testTiming)
                }
            } while (cursor.moveToNext())
            cursor.close()
        }
    }

    private fun getRandomInt(max: Int): Int {
        return (Math.random() * max).roundToInt()
    }

    private fun randomDateTime(): Long {
        val startYear = 2023
        val endYear = 2024

        val sec = getRandomInt(59)
        val min = getRandomInt(59)
        val hour = getRandomInt(23)
        val month = getRandomInt(11)

        val year = startYear + getRandomInt(endYear - startYear)
        val gc = GregorianCalendar(year, month, 1)
        val day = 1 + getRandomInt(gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH) - 1)
        gc.set(year, month, day, hour, min, sec)
        return gc.timeInMillis
    }

    private fun saveCurrentTiming(contentResolver: ContentResolver, currentTiming: TestTiming) {
        val value = ContentValues()
        value.put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
        value.put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
        value.put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
        CoroutineScope(Dispatchers.IO).launch {
            contentResolver.insert(TimingsContract.CONTENT_URI, value)
        }
    }

}