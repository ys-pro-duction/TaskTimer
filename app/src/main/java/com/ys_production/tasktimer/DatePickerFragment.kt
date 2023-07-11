package com.ys_production.tasktimer

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatDialogFragment
import java.util.Date
import java.util.GregorianCalendar

private const val TAG = "DatePickerFragment"
const val DATE_PICKER_ID = "ID"
const val DATE_PICKER_TITLE = "TITLE"
const val DATE_PICKER_DATE = "DATE"
const val DATE_PICKER_FDOW = "FIRST_DAY_OF_WEEK"

@Suppress("DEPRECATION")
class DatePickerFragment: AppCompatDialogFragment(), DatePickerDialog.OnDateSetListener {
    private var dialogId = 0
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val cal = GregorianCalendar()
        var title: String? = null
        val arguments = arguments
        if (arguments != null){
            dialogId = arguments.getInt(DATE_PICKER_ID)
            title = arguments.getString(DATE_PICKER_TITLE)

            val givenDate = arguments.getSerializable(DATE_PICKER_DATE) as Date?
            if (givenDate != null){
                cal.time = givenDate
                Log.d(TAG, "onCreateDialog: retrived date: $givenDate")
            }
        }
        val year = cal.get(GregorianCalendar.YEAR)
        val month = cal.get(GregorianCalendar.MONTH)
        val day = cal.get(GregorianCalendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(requireContext(),this,year,month,day)
        if (title != null){
            dpd.setTitle(title)
        }
        val firstDayOfWeek = arguments?.getInt(DATE_PICKER_FDOW) ?: cal.firstDayOfWeek
        dpd.datePicker.firstDayOfWeek = firstDayOfWeek
        return dpd
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is DatePickerDialog.OnDateSetListener){
            throw IllegalStateException("context !is DatePickerDialog.OnDateSetListener")
        }
    }
    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        Log.d(TAG, "onDateSet: start")
        view.tag = dialogId
        (context as DatePickerDialog.OnDateSetListener?)?.onDateSet(view,year, month, dayOfMonth)
    }
}