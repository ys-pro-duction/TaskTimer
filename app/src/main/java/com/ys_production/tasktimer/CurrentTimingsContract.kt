package com.ys_production.tasktimer

import android.net.Uri

object CurrentTimingsContract {
    internal const val TABLE_NAME = "vwCurrentTiming"
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    object Columns {
        const val TIMING_ID = TimingsContract.Columns.ID
        const val TASK_ID = TimingsContract.Columns.TIMING_TASK_ID
        const val START_TIME = TimingsContract.Columns.TIMING_START_TIME
        const val TASK_NAME = TasksContract.Columns.TASK_NAME
    }
}