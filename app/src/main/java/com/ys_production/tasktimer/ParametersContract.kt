package com.ys_production.tasktimer

import android.content.ContentUris
import android.net.Uri

object ParametersContract {
    const val ID_SHORT_TIMING = 0L

    internal const val TABLE_NAME = "Parameters"
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)
    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    object Column {
        const val ID = "_id"
        const val VALUE = "Value"
    }

    fun getId(uri: Uri): Long {
        return ContentUris.parseId(uri)
    }

    fun getUriFromId(id: Long): Uri {
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }
}