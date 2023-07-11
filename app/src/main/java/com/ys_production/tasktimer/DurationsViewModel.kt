package com.ys_production.tasktimer

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.GregorianCalendar

enum class SortColumn {
    NAME, DESCRIPTION, START_DATE, DURATION
}

private const val TAG = "DurationsViewModel"

class DurationsViewModel(application: Application) : AndroidViewModel(application) {
    private var _displayWeek = true
    val displayWeek: Boolean get() = _displayWeek
    private val databaseCursor = MutableLiveData<Cursor?>()
    val cursor: LiveData<Cursor?> get() = databaseCursor
    private var calendar = GregorianCalendar()

    var shortOrder = SortColumn.NAME
        set(order) {
            field = order
            loadData()
        }
    private val selection = "${DurationContract.Columns.START_TIME} Between ? AND ?"
    private var selectionArgs = arrayOf<String>()
    private val cursorObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.d(TAG, "onChange: start $uri")
            loadData()
        }
    }
    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == Intent.ACTION_TIMEZONE_CHANGED || action == Intent.ACTION_LOCALE_CHANGED) {
                Log.d(TAG, "onReceive: intent: $intent")
                val currentTime = calendar.timeInMillis
                calendar = GregorianCalendar()
                calendar.timeInMillis = currentTime
                _firstDayOfWeek =
                    settings.getInt(SETTINGS_FIRST_DAY_OF_WEEK, calendar.firstDayOfWeek)
                Log.d(TAG, "onReceive: $SETTINGS_FIRST_DAY_OF_WEEK: $firstDayOfWeek")
                calendar.firstDayOfWeek = firstDayOfWeek
                applyFilter()
            }
        }
    }
    private val settings =
        application.applicationContext.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    private var _firstDayOfWeek =
        settings.getInt(SETTINGS_FIRST_DAY_OF_WEEK, calendar.firstDayOfWeek)
    val firstDayOfWeek get() = _firstDayOfWeek
    private val settingsListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when(key){
            SETTINGS_FIRST_DAY_OF_WEEK -> {
                _firstDayOfWeek = sharedPreferences.getInt(key,calendar.firstDayOfWeek)
                calendar.firstDayOfWeek = firstDayOfWeek
                Log.d(TAG, "settingsListener: firstDayOfWeek: $_firstDayOfWeek $firstDayOfWeek")
                applyFilter()
            }
        }
    }

    init {
        Log.d(TAG, "DVM init: $SETTINGS_FIRST_DAY_OF_WEEK: $firstDayOfWeek")
        calendar.firstDayOfWeek = firstDayOfWeek
        val intentFilter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED)
        application.apply {
            registerReceiver(broadCastReceiver, intentFilter)
            contentResolver.registerContentObserver(
                TimingsContract.CONTENT_URI, true, cursorObserver
            )
        }
        settings.registerOnSharedPreferenceChangeListener(settingsListener)

        applyFilter()
    }

    private fun loadData() {
        val order = when (shortOrder) {
            SortColumn.NAME -> DurationContract.Columns.NAME
            SortColumn.DESCRIPTION -> DurationContract.Columns.DESCRIPTION
            SortColumn.START_DATE -> DurationContract.Columns.START_TIME
            SortColumn.DURATION -> DurationContract.Columns.DURATION
        }
        Log.d(TAG, "loadData: $order")
        CoroutineScope(Dispatchers.Default).launch {
            val cursor = getApplication<Application>().contentResolver.query(
                DurationContract.CONTENT_URI, null, selection, // {selection}
                selectionArgs, // {selectionArgs}
                order
            )
            databaseCursor.postValue(cursor)
        }
    }

    fun toggleDisplayWeek() {
        _displayWeek = !_displayWeek
        applyFilter()
    }

    fun getFilterDate(): Date {
        return calendar.time
    }

    fun setReportDate(year: Int, month: Int, dayOfMonth: Int) {
        if (calendar.get(GregorianCalendar.YEAR) != year || calendar.get(GregorianCalendar.MONTH) != month || calendar.get(
                GregorianCalendar.DAY_OF_MONTH
            ) != dayOfMonth
        ) {
            calendar.set(year, month, dayOfMonth)
            applyFilter()
        }
    }

    private fun applyFilter() {
        Log.d(TAG, "applyFilter: start")
        val currentCalendarDate = calendar.timeInMillis
        if (displayWeek) {
            val weekStart = calendar.firstDayOfWeek
            calendar.set(GregorianCalendar.DAY_OF_WEEK, weekStart)
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)
            val startDate = calendar.timeInMillis / 1000

            calendar.set(GregorianCalendar.DAY_OF_WEEK, 7)
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 23)
            calendar.set(GregorianCalendar.MINUTE, 59)
            calendar.set(GregorianCalendar.SECOND, 59)
            val endDate = calendar.timeInMillis / 1000
            selectionArgs = arrayOf(startDate.toString(), endDate.toString())
            Log.d(TAG, "applyFilter: applyFiler(7) startTime: $startDate endTime: $endDate")
        } else {
            calendar.set(GregorianCalendar.HOUR_OF_DAY, 0)
            calendar.set(GregorianCalendar.MINUTE, 0)
            calendar.set(GregorianCalendar.SECOND, 0)
            val startDate = calendar.timeInMillis / 1000

            calendar.set(GregorianCalendar.HOUR_OF_DAY, 23)
            calendar.set(GregorianCalendar.MINUTE, 59)
            calendar.set(GregorianCalendar.SECOND, 59)
            val endDate = calendar.timeInMillis / 1000
            selectionArgs = arrayOf(startDate.toString(), endDate.toString())
            Log.d(TAG, "applyFilter: applyFiler(1) startTime: $startDate endTime: $endDate")
        }
        calendar.timeInMillis = currentCalendarDate
        loadData()
    }

    fun deleteRecord(timeInMillis: Long) {
        Log.d(TAG, "deleteRecord: $timeInMillis")
        val longDate = timeInMillis / 1000
        val selectionArgs = arrayOf(longDate.toString())
        val selection = "${TimingsContract.Columns.TIMING_START_TIME} < ?"
        GlobalScope.launch {
            getApplication<Application>().contentResolver.delete(
                TimingsContract.CONTENT_URI, selection, selectionArgs
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().apply {
            contentResolver.unregisterContentObserver(cursorObserver)
            unregisterReceiver(broadCastReceiver)
        }
        settings.unregisterOnSharedPreferenceChangeListener(settingsListener)
    }
}