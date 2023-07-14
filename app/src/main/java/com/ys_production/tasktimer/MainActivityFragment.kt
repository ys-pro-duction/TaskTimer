package com.ys_production.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ys_production.tasktimer.databinding.FragmentMainActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivityFragment"

class MainActivityFragment : Fragment(), CursorAdapter.OnTaskClickListener {
    private val binding by lazy { FragmentMainActivityBinding.inflate(layoutInflater) }
    private val viewModel: TaskTimerViewModel by activityViewModels()
    private val adapter = CursorAdapter(null, this)

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.ffRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ffRecyclerView.adapter = adapter
        val itemSwipeHelper =
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if (direction == ItemTouchHelper.LEFT) {
                        val task = (viewHolder as CursorViewHolder).task
                        if (viewModel.editedTaskId == task.id.toLong()){
                            adapter.notifyItemChanged(viewHolder.adapterPosition)
                            Toast.makeText(context,getString(R.string.can_not_delete_toast), Toast.LENGTH_LONG).show()
                        }else {
                            onDeleteClick(task, viewHolder.adapterPosition)
                        }
                    }
                }
            })
        itemSwipeHelper.attachToRecyclerView(binding.ffRecyclerView)
        viewModel.cursor.observe(viewLifecycleOwner) {
            val cursor = adapter.swapCursor(it)
            CoroutineScope(Dispatchers.IO).launch { cursor?.close() }
        }
        viewModel.timing.observe(viewLifecycleOwner) {
            binding.itemSelected.text = if (it != null) {
                getString(R.string.currently_timing, it)
            } else {
                getString(R.string.no_task_selected)
            }
        }
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is OnTaskEdit) throw Exception("context is not OnTaskEdit")
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit?)?.onTaskEdit(task)
    }

    fun onDeleteClick(task: Task, position: Int) {
        Log.d(TAG, "onDeleteClick: ")
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.delete_task_massage, task.name))
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTask(task)
            }.setNegativeButton("Cancel") { _, _ ->
                adapter.notifyItemChanged(position)
            }.create().show()
    }

    override fun onTaskBackLongClick(task: Task) {
        Log.d(TAG, "onTaskBackLongClick: ")
        viewModel.timeTask(task)
    }
}