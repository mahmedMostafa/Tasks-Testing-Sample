package com.alien.brainean.todoapp.tasks

import android.os.Bundle
import android.view.*
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.alien.brainean.todoapp.MyApplication
import com.alien.brainean.todoapp.R
import com.alien.brainean.todoapp.databinding.FragmentTasksBinding
import com.alien.brainean.todoapp.ui.MainActivity
import com.alien.brainean.todoapp.util.Event
import com.alien.brainean.todoapp.util.EventObserver
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 */
class TasksFragment : Fragment() {

    private val viewModel by viewModels<TasksViewModel> {
        TasksViewModelFactory((requireContext().applicationContext as MyApplication).taskRepository)
    }

    private lateinit var binding: FragmentTasksBinding
    private lateinit var adapter: TasksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTasksBinding.inflate(inflater, container, false).apply {
            viewModel = this@TasksFragment.viewModel
        }
        init()
        return binding.root
    }

    private fun init() {
        setupToolbar()
        setupRecyclerView()
        setupFab()
        subscribeToObservers()
        setupChipGroup(binding.root)
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        if (activity is MainActivity) {
            (activity as AppCompatActivity).setSupportActionBar(binding.toolBar)
        }
    }

    private fun setupChipGroup(view: View) {
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group)
        chipGroup.setOnCheckedChangeListener { chipGroup, checkedId ->
            val chip = chipGroup.findViewById<Chip>(checkedId)
            chip?.let {
                when (chip.text) {
                    "All Tasks" -> viewModel.setFilter(TasksFilterType.ALL_TASKS)
                    "Completed" -> viewModel.setFilter(TasksFilterType.COMPLETED_TASKS)
                    "Active" -> viewModel.setFilter(TasksFilterType.ACTIVE_TASKS)
                }
            }
        }
    }

    private fun subscribeToObservers() {
        viewModel.snackbarMessage.observe(viewLifecycleOwner, EventObserver { message ->
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
        })

        viewModel.update.observe(viewLifecycleOwner, Observer { update ->
            Timber.d("Gemy update has changed")
        })

        viewModel.tasks.observe(viewLifecycleOwner, Observer { tasks ->
            Timber.d("Gemy tasks changed")
            tasks?.let {
                adapter.submitList(tasks)
            }
            if (tasks.isEmpty()) {
                binding.emptyLayout.visibility = View.VISIBLE
            } else {
                binding.emptyLayout.visibility = View.GONE
            }
        })

        viewModel.triggerNavigation.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(
                TasksFragmentDirections.actionTasksFragmentToDetailFragment(it)
            )
        })
    }

    private fun setupFab() {
        binding.addTaskFab.setOnClickListener {
            val action: String? = null
            findNavController().navigate(
                TasksFragmentDirections.actionTasksFragmentToDetailFragment("new")
            )
        }
    }

    private fun setupRecyclerView() {
        if (binding.viewModel != null) {
            adapter = TasksAdapter(viewModel)
            binding.recyclerView.adapter = adapter
        } else {
            Timber.e("ViewModel hasn't been initialized yet")
        }
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.deleteTask(adapter.getTaskAt(viewHolder.adapterPosition))
                Toast.makeText(activity, "Task deleted successfully", Toast.LENGTH_LONG).show()
            }
        }).attachToRecyclerView(binding.recyclerView)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.all_tasks_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_clear_completed_tasks -> {
                viewModel.clearCompletedTasks()
                true
            }

            R.id.action_delete_all_tasks -> {
                viewModel.deleteAllTasks()
                true
            }

            else -> false
        }
    }
}
