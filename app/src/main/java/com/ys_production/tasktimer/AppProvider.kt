package com.ys_production.tasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

private const val TAG = "AppProvider"
const val CONTENT_AUTHORITY = "com.ys_production.tasktimer.provider"
private const val TASKS = 100
private const val TASKS_ID = 101
private const val TIMINGS = 200
private const val TIMINGS_ID = 201
private const val CURRENT_TIMING = 300
private const val TASK_DURATION = 400
private const val PARAMETERS = 500
private const val PARAMETER_ID = 500
val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {
    private val uriMatcher by lazy { buildUriMatcher() }
    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "buildUriMatcher: start")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
        // e.g. content://com.ys_production.tasktimer.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)
        // e.g. content://com.ys_production.tasktimer.provider/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME + "/#", TASKS_ID)
        // e.g. content://com.ys_production.tasktimer.provider/Timings
        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS)
        // e.g. content://com.ys_production.tasktimer.provider/Timings/8
        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME + "/#", TIMINGS_ID)
        matcher.addURI(CONTENT_AUTHORITY, CurrentTimingsContract.TABLE_NAME, CURRENT_TIMING)
        matcher.addURI(CONTENT_AUTHORITY, DurationContract.TABLE_NAME, TASK_DURATION)
        matcher.addURI(CONTENT_AUTHORITY, ParametersContract.TABLE_NAME, PARAMETERS)
        matcher.addURI(CONTENT_AUTHORITY, ParametersContract.TABLE_NAME + "/#", PARAMETER_ID)
        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: start")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        Log.d(TAG, "query: start query uri: $uri")
        val match = uriMatcher.match(uri)
        val queryBuilder = SQLiteQueryBuilder()
        when (match) {
            TASKS -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
            }

            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val id = TasksContract.getId(uri)
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$id")
            }

            TIMINGS -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
            }

            TIMINGS_ID -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
                val id = TimingsContract.getId(uri)
                queryBuilder.appendWhere("${TimingsContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$id")
            }

            CURRENT_TIMING -> queryBuilder.tables = CurrentTimingsContract.TABLE_NAME
            TASK_DURATION -> queryBuilder.tables = DurationContract.TABLE_NAME

            PARAMETERS -> {
                queryBuilder.tables = ParametersContract.TABLE_NAME
            }

            PARAMETER_ID -> {
                queryBuilder.tables = ParametersContract.TABLE_NAME
                val id = ParametersContract.getId(uri)
                queryBuilder.appendWhere("${ParametersContract.Column.ID} = ")
                queryBuilder.appendWhereEscapeString("$id")
            }
            else -> throw IllegalStateException("unknown uri: $uri")
        }
        val db = AppDatabase.getInstance(context!!).readableDatabase
        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query cursor count: ${cursor.count}")
        return cursor
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            TASKS -> TasksContract.CONTENT_TYPE
            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE
            TIMINGS -> TimingsContract.CONTENT_TYPE
            TIMINGS_ID -> TimingsContract.CONTENT_ITEM_TYPE
            CURRENT_TIMING -> CurrentTimingsContract.CONTENT_ITEM_TYPE
            TASK_DURATION -> DurationContract.CONTENT_ITEM_TYPE
            PARAMETERS -> ParametersContract.CONTENT_TYPE
            PARAMETER_ID -> ParametersContract.CONTENT_ITEM_TYPE
            else -> throw IllegalStateException("error in gettype uri: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        Log.d(TAG, "insert: start insert uri: $uri")
        val match = uriMatcher.match(uri)
        val returnId: Long
        val returnUri: Uri?
        val context = context!!
        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                returnId = db.insert(TasksContract.TABLE_NAME, null, values)
                returnUri = TasksContract.getUriFromId(returnId)
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                returnId = db.insert(TimingsContract.TABLE_NAME, null, values)
                returnUri = TimingsContract.getUriFromId(returnId)
            }

            else -> throw IllegalStateException("error unknown uri: $uri")
        }
        if (returnId == -1L) {
            throw IllegalStateException("error unknown uri: $uri")
        } else {
            if (returnId > 0) {
                Log.d(TAG, "insert: observer call $uri")
                context.contentResolver.notifyChange(uri, null)
            }
        }
        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete: start delete uri: $uri")
        val match = uriMatcher.match(uri)
        var selectionCriteria: String
        val rowAffected: Int
        val context = context!!
        val db = AppDatabase.getInstance(context).writableDatabase
        when (match) {
            TASKS -> {
                rowAffected = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }

            TASKS_ID -> {
                selectionCriteria = "${TasksContract.Columns.ID} = ${TasksContract.getId(uri)}"
                selection?.let { if (it.isNotEmpty()) selectionCriteria += "AND $it" }
                rowAffected = db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            TIMINGS -> {
                rowAffected = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                selectionCriteria = "${TimingsContract.Columns.ID} = ${TimingsContract.getId(uri)}"
                selection?.let { if (it.isNotEmpty()) selectionCriteria += "AND $it" }
                rowAffected =
                    db.delete(TimingsContract.TABLE_NAME, selectionCriteria, selectionArgs)
            }

            else -> throw IllegalStateException("error on delete unknown uri: $uri")
        }
        if (rowAffected > 0) {
            Log.d(TAG, "delete: observer call $uri")
            context.contentResolver?.notifyChange(uri, null)
        }
        return rowAffected
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int {
        Log.d(TAG, "update: start update uri: $uri")
        val match = uriMatcher.match(uri)
        var selectionCriteria: String
        val rowAffected: Int
        val context = context!!
        val db = AppDatabase.getInstance(context).writableDatabase
        when (match) {
            TASKS -> {
                rowAffected = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TASKS_ID -> {
                selectionCriteria = "${TasksContract.Columns.ID} = ${TasksContract.getId(uri)}"
                selection?.let { if (it.isNotEmpty()) selectionCriteria += "AND $it" }
                rowAffected = db.update(
                    TasksContract.TABLE_NAME, values, selectionCriteria, selectionArgs
                )
            }

            TIMINGS -> {
                rowAffected = db.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                selectionCriteria = "${TimingsContract.Columns.ID} = ${TimingsContract.getId(uri)}"
                selection?.let { if (it.isNotEmpty()) selectionCriteria += "AND $it" }
                Log.d(TAG, "update: slectioncriteria: $selectionCriteria")
                rowAffected = db.update(
                    TimingsContract.TABLE_NAME, values, selectionCriteria, selectionArgs
                )
            }
            PARAMETER_ID -> {
                val id = ParametersContract.getId(uri)
                selectionCriteria = "${ParametersContract.Column.ID} = $id"
                selection?.let { if (it.isNotEmpty()) selectionCriteria += "AND $it" }
                Log.d(TAG, "update: slectioncriteria: $selectionCriteria")
                rowAffected = db.update(
                    ParametersContract.TABLE_NAME, values, selectionCriteria, selectionArgs
                )
            }

            else -> throw IllegalStateException("error on update unknown uri: $uri")
        }
        if (rowAffected > 0) {
            Log.d(TAG, "insert: observer call $uri")
            context.contentResolver?.notifyChange(uri, null)
        }
        return rowAffected
    }
}