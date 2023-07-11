package com.ys_production.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialogFragment
import java.util.GregorianCalendar
import java.util.Locale

private const val TAG = "SettingsDialog"
const val SETTINGS_FIRST_DAY_OF_WEEK = "FirstDay"
const val SETTINGS_IGNORE_LESS_THEN = "ignoreLessThan"
const val SETTINGS_DEFALUT_IGNORE_LESS_THAN = 0
private val DAY_IN_WEEK =
    arrayOf( "Sunday", "Monday", "Tuesday", "Wenesday", "Thrusday", "Friday", "Saturday" )
//                              0 1 2  3  4  5  6  7  8  9  10 11 12 13   14  15 16  17   18  19  20  21  22  23   24
private val deltas = intArrayOf(0,5,10,15,20,25,30,35,40,45,50,55,60,120,180,240,300,360,420,480,540,600,900,1800,2700)
class SettingsDialog : AppCompatDialogFragment() {
    private val defaultFirstDayOfWeek = GregorianCalendar(Locale.getDefault()).firstDayOfWeek
    private var firstDayOfWeek = defaultFirstDayOfWeek
    private var ignoreLessThan = SETTINGS_DEFALUT_IGNORE_LESS_THAN


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.SettingsDialogStyle)
        retainInstance = true
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setTitle(R.string.action_settings)
        return layoutInflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.settings_ok_btn).setOnClickListener {
            saveValues()
            dismiss()
        }
        view.findViewById<Button>(R.id.settings_cancel_btn).setOnClickListener {
            dismiss()
        }
        view.findViewById<SeekBar>(R.id.seekBar).setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    if (progress < 12){
                        view.findViewById<TextView>(R.id.settingsIgnoreSecondsTitle).text = getString(R.string.settings_timing,
                            deltas[progress],resources.getQuantityString(R.plurals.settingsLittleUnits,deltas[progress]))
                    }else{
                        view.findViewById<TextView>(R.id.settingsIgnoreSecondsTitle).text = getString(R.string.settings_timing,
                            (deltas[progress]/60),resources.getQuantityString(R.plurals.settingsBigUnits,(deltas[progress]/60)))
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
    private fun readValues(){
        context?.getSharedPreferences("Settings",Context.MODE_PRIVATE)?.let {
            firstDayOfWeek = it.getInt(SETTINGS_FIRST_DAY_OF_WEEK,defaultFirstDayOfWeek)
            ignoreLessThan = it.getInt(SETTINGS_IGNORE_LESS_THEN, SETTINGS_DEFALUT_IGNORE_LESS_THAN)
        }
        Log.d(TAG, "readValues: firstDayOfWeek = $firstDayOfWeek, ignoreLessThan = $ignoreLessThan")
    }
    private fun saveValues() {
        val view = view
        if (view != null) {
            val newFirstDay =
                view.findViewById<Spinner>(R.id.settings_spinner).selectedItemPosition + GregorianCalendar.SUNDAY
            val newIgnoreLessThen = deltas[view.findViewById<SeekBar>(R.id.seekBar).progress]
            Log.d(
                TAG,
                "saveValues: newFirst day $newFirstDay, newIgnoreLessThen $newIgnoreLessThen"
            )
            requireContext().applicationContext.getSharedPreferences("Settings", Context.MODE_PRIVATE).edit().apply {
                if (newFirstDay != firstDayOfWeek) putInt(SETTINGS_FIRST_DAY_OF_WEEK, newFirstDay)
                if (newIgnoreLessThen != ignoreLessThan) putInt(SETTINGS_IGNORE_LESS_THEN, newIgnoreLessThen)
            }.apply()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            view?.findViewById<SeekBar>(R.id.seekBar)?.progress?.let {
                val deltaValue = deltas[it]
                if (deltaValue < 60){
                    view?.findViewById<TextView>(R.id.settingsIgnoreSecondsTitle)?.text = getString(R.string.settings_timing,
                        (deltaValue),resources.getQuantityString(R.plurals.settingsLittleUnits,(deltaValue)))
                }else{
                    view?.findViewById<TextView>(R.id.settingsIgnoreSecondsTitle)?.text = getString(R.string.settings_timing,
                        (deltaValue/60),resources.getQuantityString(R.plurals.settingsBigUnits,(deltaValue/60)))
                }
            }
            return
        }
        readValues()
        val deltaPosition = deltas.binarySearch(ignoreLessThan)
        view?.findViewById<SeekBar>(R.id.seekBar)?.progress = deltaPosition
        view?.findViewById<Spinner>(R.id.settings_spinner)?.setSelection(firstDayOfWeek-GregorianCalendar.SUNDAY)
        if (ignoreLessThan < 60){
            view?.findViewById<TextView>(R.id.settingsIgnoreSecondsTitle)?.text = getString(R.string.settings_timing,
                (ignoreLessThan),resources.getQuantityString(R.plurals.settingsLittleUnits,(ignoreLessThan)))
        }else{
            view?.findViewById<TextView>(R.id.settingsIgnoreSecondsTitle)?.text = getString(R.string.settings_timing,
                (ignoreLessThan/60),resources.getQuantityString(R.plurals.settingsBigUnits,(ignoreLessThan/60)))
        }
    }
}