package com.ys_production.tasktimer

import android.content.ContentUris
import android.net.Uri

object TimingsContract {
    internal const val TABLE_NAME = "Timings"
    val CONTENT_URI = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)
    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    object Columns{
        const val ID = "_id"
        const val TIMING_TASK_ID = "TaskId"
        const val TIMING_START_TIME = "StartTime"
        const val TIMING_DURATION = "Duration"
    }
    fun getId(uri: Uri): Long{
        return ContentUris.parseId(uri)
    }
    fun getUriFromId(id: Long): Uri{
        return ContentUris.withAppendedId(CONTENT_URI,id)
    }
}