package com.ys_production.tasktimer

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.util.GregorianCalendar

private const val TAG = "DurationsReport"
const val DIALOG_FILTER = 1
const val DIALOG_DELETE = 2

class DurationsReport : AppCompatActivity(), View.OnClickListener,
    DatePickerDialog.OnDateSetListener {
    private val reportAdapter by lazy { DurationRVAdapter(this, null) }
    private val viewModel by lazy { ViewModelProvider(this)[DurationsViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_durations_report)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val rv = findViewById<RecyclerView>(R.id.td_list)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = reportAdapter
        findViewById<TextView>(R.id.td_task_name_heading).setOnClickListener(this)
        (findViewById<TextView>(R.id.td_task_description_heading))?.setOnClickListener(this)
        findViewById<TextView>(R.id.td_start_time_heading).setOnClickListener(this)
        findViewById<TextView>(R.id.td_task_duration_heading).setOnClickListener(this)
        viewModel.cursor.observe(this) { cursor -> reportAdapter.swapCursor(cursor)?.close() }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.td_task_name_heading -> viewModel.shortOrder = SortColumn.NAME
            R.id.td_task_description_heading -> viewModel.shortOrder = SortColumn.DESCRIPTION
            R.id.td_start_time_heading -> viewModel.shortOrder = SortColumn.START_DATE
            R.id.td_task_duration_heading -> viewModel.shortOrder = SortColumn.DURATION
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.duration_report, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.rm_filter_period -> {
                viewModel.toggleDisplayWeek()
                invalidateMenu()
                return true
            }

            R.id.rm_filter_date -> {
                showDatePickerFragment(getString(R.string.date_title_filter), DIALOG_FILTER)
                return true
            }

            R.id.rm_delete -> {
                showDatePickerFragment(getString(R.string.date_title_delete), DIALOG_DELETE)
                return true
            }
            R.id.rm_change_settings -> {
                val sd = SettingsDialog()
                sd.show(supportFragmentManager,"settings")
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.rm_filter_period)
        if (item != null) {
            if (viewModel.displayWeek) {
                setMenuIcon(item, R.drawable.baseline_filter_1_24, R.string.rm_title_filter_day)
            } else {
                setMenuIcon(item, R.drawable.baseline_filter_7_24, R.string.rm_title_filter_week)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setMenuIcon(item: MenuItem, icon: Int, title: Int) {
        item.setIcon(icon)
        item.setTitle(title)
    }

    private fun showDatePickerFragment(title: String, id: Int) {
        val arguments = Bundle()
        arguments.putInt(DATE_PICKER_ID, id)
        arguments.putString(DATE_PICKER_TITLE, title)
        arguments.putSerializable(DATE_PICKER_DATE, viewModel.getFilterDate())
        arguments.putInt(DATE_PICKER_FDOW,viewModel.firstDayOfWeek)
        val dialog = DatePickerFragment()
        dialog.arguments = arguments
        dialog.show(supportFragmentManager, "datePicker")
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        Log.d(TAG, "onDateSet: start")
        val dialogId = view.tag
        when (dialogId) {
            DIALOG_FILTER -> {
                viewModel.setReportDate(year, month, dayOfMonth)
            }

            DIALOG_DELETE -> {
                val cal = GregorianCalendar()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                val fromDate = DateFormat.getDateInstance().format(cal.timeInMillis)
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_ask_title))
                    .setMessage(getString(R.string.delete_ask_message, fromDate))
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteRecord(cal.timeInMillis)
                    }.setNegativeButton("Cancel") { _, _ ->

                    }.create().show()
            }

            else -> throw TypeNotPresentException("dialogId", Throwable("dialogid is $dialogId"))
        }
    }
//    override fun onDestroy() {
//        reportAdapter.swapCursor(null)?.close()
//        super.onDestroy()
//    }
}