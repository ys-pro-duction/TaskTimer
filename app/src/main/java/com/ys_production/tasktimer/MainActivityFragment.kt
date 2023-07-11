package com.ys_production.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ys_production.tasktimer.databinding.FragmentMainActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MainActivityFragment"

class MainActivityFragment : Fragment(), CursorAdapter.OnTaskClickListener {
    private val bindding by lazy { FragmentMainActivityBinding.inflate(layoutInflater) }
    private val viewModel by lazy { ViewModelProvider(this)[TaskTimerViewModel::class.java] }
    private val adapter = CursorAdapter(null, this)

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        bindding.ffRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        bindding.ffRecyclerView.adapter = adapter
        viewModel.cursor.observe(viewLifecycleOwner) {
            val cursor = adapter.swapCursor(it)
            CoroutineScope(Dispatchers.Default).launch { cursor?.close() }
        }
        viewModel.timing.observe(viewLifecycleOwner){
            bindding.itemSelected.text = if (it != null){
                getString(R.string.currently_timing,it)
            }else{
                getString(R.string.no_task_selected)
            }
        }
        return bindding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context !is OnTaskEdit) throw Exception("context is not OnTaskEdit")
    }

    override fun onEditClick(task: Task) {
        (activity as OnTaskEdit?)?.onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        Log.d(TAG, "onDeleteClick: ")
        viewModel.deleteTask(task)
    }

    override fun onTaskBackLongClick(task: Task) {
        Log.d(TAG, "onTaskBackLongClick: ")
        viewModel.timeTask(task)
    }
}