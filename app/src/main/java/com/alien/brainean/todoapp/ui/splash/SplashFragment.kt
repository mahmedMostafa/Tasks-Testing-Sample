package com.alien.brainean.todoapp.ui.splash

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.alien.brainean.todoapp.MyApplication

import com.alien.brainean.todoapp.R
import com.alien.brainean.todoapp.util.setupSnackbar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

class SplashFragment : Fragment(R.layout.splash_fragment) {

    private val TIME_OUT = 4000L

    private val handler = Handler()

    private val finishSplash = Runnable {

    }

    private val viewModel by viewModels<SplashViewModel> {
        SplashViewModelFactory((requireContext().applicationContext as MyApplication).taskRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler.postDelayed(finishSplash, TIME_OUT)
        subscribeToObservers()
        setupSnackbar()
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarMessage, Snackbar.LENGTH_SHORT)
    }

    private fun subscribeToObservers() {
        viewModel.syncData()
        viewModel.triggerNavigation.observe(viewLifecycleOwner, Observer {
            Timber.d("Triggered Navigation")
            findNavController().navigate(
                SplashFragmentDirections.actionSplashFragmentToTasksFragment()
            )
        })
    }

    override fun onDestroyView() {
        handler.removeCallbacks(finishSplash)
        super.onDestroyView()
    }
}
