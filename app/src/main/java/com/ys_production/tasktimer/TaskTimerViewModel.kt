package com.ys_production.tasktimer

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor> get() = databaseCursor
    val cursorObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.d(TAG, "onChange: start $uri")
            loadTasks()
        }
    }

    init {
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI, true, cursorObserver
        )
        loadTasks()
    }

    private fun loadTasks() {
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SHORT_ORDER
        )
        val shortOrder =
            "${TasksContract.Columns.TASK_SHORT_ORDER},${TasksContract.Columns.TASK_NAME}"
        CoroutineScope(Dispatchers.Default).launch {
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI, projection, null, null, shortOrder
            )
            cursor?.let { databaseCursor.postValue(it) }
        }
    }

    fun saveTask(task: Task): Task {
        Log.d(TAG, "saveTask: start")
        if (task.name?.isNotEmpty() == true) {
            val value = ContentValues()
            value.put(TasksContract.Columns.TASK_NAME, task.name)
            value.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            value.put(TasksContract.Columns.TASK_SHORT_ORDER, task.shortOrder)
            if (task.id == 0) {
                CoroutineScope(Dispatchers.Default).launch {
                    val uri = getApplication<Application>().contentResolver.insert(
                        TasksContract.CONTENT_URI,
                        value
                    )
                    if (uri != null) {
                        task.id = TasksContract.getId(uri).toInt()
                    }
                }
            } else {
                CoroutineScope(Dispatchers.Default).launch {
                    getApplication<Application>().contentResolver.update(
                        TasksContract.getUriFromId(
                            task.id.toLong()
                        ), value, null, null
                    )
                }
            }
        }
        return task
    }

    fun deleteTask(task: Task) {
        CoroutineScope(Dispatchers.Default).launch {
            getApplication<Application>().contentResolver.delete(
                TasksContract.getUriFromId(task.id.toLong()), null, null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(cursorObserver)
    }
}