package com.ys_production.tasktimer

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor> get() = databaseCursor
    private var currentTiming: Timing? = null
    private val taskTiming = MutableLiveData<String?>()
    val optionsMenu = MutableLiveData(true)
    val timing: LiveData<String?> get() = taskTiming
    var editedTaskId = 0L
        private set

    private val cursorObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.d(TAG, "onChange: start $uri")
            loadTasks()
        }
    }
    private var settings = application.applicationContext.getSharedPreferences(
        "Settings", Context.MODE_PRIVATE
    )
    private var ignoreLessThan = settings.getInt(
        SETTINGS_IGNORE_LESS_THEN, SETTINGS_DEFALUT_IGNORE_LESS_THAN
    )
    private val settingsListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                SETTINGS_IGNORE_LESS_THEN -> {
                    ignoreLessThan =
                        sharedPreferences.getInt(key, SETTINGS_DEFALUT_IGNORE_LESS_THAN)
                    Log.d(TAG, "settingsListener: ignoreLessThan: $ignoreLessThan")
                    val value = ContentValues()
                    value.put(ParametersContract.Column.VALUE, ignoreLessThan)
                    viewModelScope.launch(Dispatchers.IO) {
                        val uri = application.contentResolver.update(
                            ParametersContract.getUriFromId(ParametersContract.ID_SHORT_TIMING),
                            value,
                            null,
                            null
                        )
                        Log.d(TAG, "settingsListener: uri: $uri")
                    }
                }
            }
        }

    init {
        application.contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI, true, cursorObserver
        )
        settings.registerOnSharedPreferenceChangeListener(settingsListener)
        loadTasks()
        currentTiming = retriveTiming()
    }

    private fun loadTasks() {
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SHORT_ORDER
        )
        val shortOrder = "${TasksContract.Columns.TASK_SHORT_ORDER},${TasksContract.Columns.TASK_NAME} COLLATE NOCASE"
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI, projection, null, null, shortOrder
            )
            cursor?.let { databaseCursor.postValue(it) }
        }
    }

    fun timeTask(task: Task) {
        Log.d(TAG, "timeTask: start")
        val timingRecord = currentTiming
        if (timingRecord == null) {
            currentTiming = Timing(task.id)
            saveTiming(currentTiming!!)
        } else {
            timingRecord.setDuration()
            saveTiming(timingRecord)
            currentTiming = if (task.id == timingRecord.taskId) {
                null
            } else {
                val newTiming = Timing(task.id)
                saveTiming(newTiming)
                newTiming
            }
        }
        taskTiming.value = if (currentTiming != null) task.name else null
    }

    private fun saveTiming(currentTiming: Timing) {
        Log.d(TAG, "saveTiming: start: ${currentTiming.duration}")
        val inseting = currentTiming.duration == 0L
        val values = ContentValues().apply {
            if (inseting) {
                put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
            }
            put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (inseting) {
                Log.d(TAG, "saveTiming: inseting")
                val uri = getApplication<Application>().contentResolver.insert(
                    TimingsContract.CONTENT_URI, values
                )
                Log.d(TAG, "saveTiming: uri $uri")
                if (uri != null) {
                    currentTiming.id = TimingsContract.getId(uri)
                }
            } else {
                Log.d(
                    TAG,
                    "saveTiming: updating:${values.getAsInteger(TimingsContract.Columns.TIMING_DURATION)}"
                )
                getApplication<Application>().contentResolver.update(
                    TimingsContract.getUriFromId(
                        currentTiming.id
                    ), values, null, null
                )
            }
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
                viewModelScope.launch(Dispatchers.IO) {
                    val uri = getApplication<Application>().contentResolver.insert(
                        TasksContract.CONTENT_URI, value
                    )
                    if (uri != null) {
                        task.id = TasksContract.getId(uri).toInt()
                    }
                }
            } else {
                viewModelScope.launch(Dispatchers.IO) {
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

    fun startEditing(taskId: Long) {
        if (BuildConfig.DEBUG && editedTaskId != 0L) {
            throw IllegalStateException("startEditing called without stopping previus edit. editTaskId: $editedTaskId, taskId: $taskId")
        } else {
            editedTaskId = taskId
        }
    }

    fun stopEditing() {
        editedTaskId = 0L
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            getApplication<Application>().contentResolver.delete(
                TasksContract.getUriFromId(task.id.toLong()), null, null
            )
        }
        if (currentTiming?.taskId == task.id) {
            currentTiming = null
            taskTiming.value = null
        }
    }

    @SuppressLint("Range")
    private fun retriveTiming(): Timing? {
        Log.d(TAG, "retriveTiming: start")
        val timing: Timing?
        val timingCursor = getApplication<Application>().contentResolver.query(
            CurrentTimingsContract.CONTENT_URI, null, null, null, null
        )
        if (timingCursor != null && timingCursor.moveToFirst()) {
            val id =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingsContract.Columns.TIMING_ID))
            val taskId =
                timingCursor.getInt(timingCursor.getColumnIndex(CurrentTimingsContract.Columns.TASK_ID))
            val startTime =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingsContract.Columns.START_TIME))
            val name =
                timingCursor.getString(timingCursor.getColumnIndex(CurrentTimingsContract.Columns.TASK_NAME))
            taskTiming.value = name
            timing = Timing(taskId, startTime, id)
        } else timing = null
        timingCursor?.close()
        return timing
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().contentResolver.unregisterContentObserver(cursorObserver)
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
        databaseCursor.value?.close()
    }
}