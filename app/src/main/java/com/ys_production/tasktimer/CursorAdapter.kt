package com.ys_production.tasktimer

import android.annotation.SuppressLint
import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CursorAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener) :
    RecyclerView.Adapter<CursorViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)
        fun onTaskBackLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursorViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_items, parent, false)
        return CursorViewHolder(view)
    }

    override fun onBindViewHolder(holder: CursorViewHolder, position: Int) {
        val cursor = cursor
        if (cursor == null || cursor.count == 0) {
            holder.title.setText(R.string.instructions_header)
            holder.des.setText(R.string.instruction)
            holder.edit.visibility = View.GONE
        } else {
            if (!cursor.moveToPosition(position)) throw Exception("cursor not move to position $position")
            val task = getTaskFromCursor(cursor)
            holder.bindData(task, listener)
        }
    }

    @SuppressLint("Range")
    private fun getTaskFromCursor(cursor: Cursor): Task {
        with(cursor) {
            val task = Task(
                getString(getColumnIndex(TasksContract.Columns.TASK_NAME)),
                getString(getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                getInt(getColumnIndex(TasksContract.Columns.TASK_SHORT_ORDER))
            )
            task.id = getInt(getColumnIndex(TasksContract.Columns.ID))
            return task
        }
    }

    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor == cursor) return null
        val oldCursor = cursor
        cursor = newCursor
        notifyDataSetChanged()
        return oldCursor
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }
}

class CursorViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.tli_title)
    val des: TextView = view.findViewById(R.id.tli_description)
    val edit: ImageButton = view.findViewById(R.id.tli_edit_btn)
    lateinit var task: Task
    fun bindData(task: Task, listener: CursorAdapter.OnTaskClickListener) {
        this.task = task
        title.text = task.name
        des.text = task.description
        edit.visibility = View.VISIBLE
        edit.setOnClickListener {
            Log.d(TAG, "binddata: edit click ${task.name}")
            listener.onEditClick(task)
        }
        view.setOnLongClickListener {
            Log.d(TAG, "binddata: back longClick ${task.name}")
            listener.onTaskBackLongClick(task)
            true
        }
    }

    companion object {
        private const val TAG = "CursorAdapter"
    }
}
