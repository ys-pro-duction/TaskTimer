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
        fun onDeleteClick(task: Task)
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
            holder.delete.visibility = View.GONE
            holder.edit.visibility = View.GONE
        }else {
            if (!cursor.moveToPosition(position)) throw Exception("cursor not move to position $position")
            val task = getTaskFromCursor(cursor)
            holder.binddata(task, listener)
        }
    }

    @SuppressLint("Range")
    private fun getTaskFromCursor(cursor: Cursor): Task {
        val task = Task(
            cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_NAME)),
            cursor.getString(cursor.getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
            cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.TASK_SHORT_ORDER))
        )
        task.id = cursor.getInt(cursor.getColumnIndex(TasksContract.Columns.ID))
        return task
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
    val title = view.findViewById<TextView>(R.id.tli_title)
    val des = view.findViewById<TextView>(R.id.tli_description)
    val delete = view.findViewById<ImageButton>(R.id.tli_delete_btn)
    val edit = view.findViewById<ImageButton>(R.id.tli_edit_btn)
    fun binddata(task: Task, listener: CursorAdapter.OnTaskClickListener) {
        title.text = task.name
        des.text = task.description
        delete.visibility = View.VISIBLE
        edit.visibility = View.VISIBLE
        delete.setOnClickListener {
            Log.d(TAG, "binddata: delete click ${task.name}")
            listener.onDeleteClick(task)
        }
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
