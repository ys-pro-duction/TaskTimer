package com.ys_production.tasktimer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val name: TextView = view.findViewById(R.id.td_task_name)
    val description: TextView? = view.findViewById(R.id.td_task_description) as TextView?
    val startTime: TextView = view.findViewById(R.id.td_start_time)
    val duration: TextView = view.findViewById(R.id.td_task_duration)
}

private const val TAG = "DurationRVAdapter"

class DurationRVAdapter(context: Context, private var cursor: Cursor?) :
    RecyclerView.Adapter<ViewHolder>() {
    private val dateFormat = DateFormat.getDateFormat(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_repost_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("Range")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cursor = cursor
        if (cursor != null && cursor.count != 0) {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("cursor not move to position $position")
            }
            val name = cursor.getString(cursor.getColumnIndex(DurationContract.Columns.NAME))
            val description =
                cursor.getString(cursor.getColumnIndex(DurationContract.Columns.DESCRIPTION))
            val duration = cursor.getLong(cursor.getColumnIndex(DurationContract.Columns.DURATION))
            val startTime =
                cursor.getLong(cursor.getColumnIndex(DurationContract.Columns.START_TIME))
            val userDate = dateFormat.format(startTime * 1000)
            val totalTime = formatDuration(duration)
            Log.d(TAG, "onBindViewHolder: name $name")
            holder.name.text = name
            holder.description?.text = description
            holder.startTime.text = userDate
            holder.duration.text = totalTime
        }
    }

    private fun formatDuration(duration: Long): String {
        val hour = duration / 3600
        val remainder = duration - (hour * 3600)
        val minutes = remainder / 60
        val seconds = remainder - (minutes * 60)
//        val seconds = minutes % 60 // same method like minutes
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minutes, seconds)
    }

    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor == cursor) return null
        val oldCursor = cursor
        cursor = newCursor
        notifyDataSetChanged()
        return oldCursor
    }

    override fun getItemCount(): Int {
        return cursor?.count ?: 0
    }
}