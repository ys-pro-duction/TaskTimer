package com.ys_production.tasktimer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ys_production.tasktimer.databinding.FragmentAddEditBinding

private const val TAG = "AddEditFragment"
private const val ARG_TASK = "task"

class AddEditFragment : Fragment() {
    interface OnSaveClicked {
        fun onSaveClicked()
    }

    private var task: Task? = null
    private var listener: OnSaveClicked? = null
    private val binding by lazy { FragmentAddEditBinding.inflate(layoutInflater) }
    private val viewModel: TaskTimerViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: start")
        arguments?.let {
            task = it.getParcelable(ARG_TASK)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.d(TAG, "onCreateView: start")
        val task = task
        if (task != null) {
            binding.addeditName.setText(task.name)
            binding.addeditDescription.setText(task.description)
            binding.addeditShort.setText(task.shortOrder.toString())

            viewModel.startEditing(task.id.toLong())
        } else {
            Log.d(TAG, "onCreateView: task is null")
        }
        binding.addeditSaveBtn.setOnClickListener {
            saveTask()
            listener?.onSaveClicked()
        }
        return binding.root
    }

    private fun taskFromUri(): Task {
        val shortOrder = if (binding.addeditShort.text.isNotEmpty()) {
            binding.addeditShort.text.toString().toInt()
        } else 0
        val newTask = Task(
            binding.addeditName.text.toString(),
            binding.addeditDescription.text.toString(),
            shortOrder
        )
        newTask.id = task?.id ?: 0
        return newTask
    }

    private fun saveTask() {
        Log.d(TAG, "saveTask: start")
        val newTask = taskFromUri()
        if (newTask != task) {
            task = viewModel.saveTask(newTask)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException(context.toString())
        }
        (context as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.optionsMenu.value = false
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        viewModel.optionsMenu.value = true
        viewModel.stopEditing()
    }

    companion object {
        @JvmStatic
        fun newInstance(task: Task?): AddEditFragment {
//            val aef = AddEditFragment()
//            val arg = Bundle()
//            arg.putParcelable(ARG_TASK, task)
//            aef.arguments = arg
//            return aef
            return AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
        }
    }
}