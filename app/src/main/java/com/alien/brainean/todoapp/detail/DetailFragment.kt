package com.alien.brainean.todoapp.detail

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.setupWithNavController
import com.alien.brainean.todoapp.MyApplication

import com.alien.brainean.todoapp.R
import com.alien.brainean.todoapp.databinding.FragmentDetailBinding
import com.alien.brainean.todoapp.ui.MainActivity
import com.alien.brainean.todoapp.util.EventObserver
import com.alien.brainean.todoapp.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class DetailFragment : Fragment() {

    private val viewModel by viewModels<DetailViewModel> {
        DetailViewModelFactory((requireContext().applicationContext as MyApplication).taskRepository)
    }

    private val args: DetailFragmentArgs by navArgs()

    private lateinit var binding: FragmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false).apply {
            this.viewmodel = viewModel
        }
        binding.lifecycleOwner = this.viewLifecycleOwner
        viewModel.start(args.taskId)
        if (activity is MainActivity) {
            setupToolbar()
        }
        subscribeToObservers()
        setupSnackbar()
        return binding.root
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)

        (activity as AppCompatActivity).setSupportActionBar(binding.toolBar)
        binding.toolBar.setupWithNavController(this.findNavController())

    }

    private fun setupSnackbar() {
        val view = view
        view?.setupSnackbar(viewLifecycleOwner, viewModel.snackbarText, Snackbar.LENGTH_SHORT)
        viewModel.snackbarText.observe(viewLifecycleOwner, Observer { event ->
            event.getContentIfNotHandled()?.let {
                Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun subscribeToObservers() {
        viewModel.triggerNavigation.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(
                DetailFragmentDirections.actionDetailFragmentToTasksFragment()
            )
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save_task) {
            viewModel.saveTask()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }
}
