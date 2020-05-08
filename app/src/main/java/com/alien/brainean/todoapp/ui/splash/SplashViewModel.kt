package com.alien.brainean.todoapp.ui.splash

import androidx.lifecycle.*
import com.alien.brainean.todoapp.data.TasksRepository
import com.alien.brainean.todoapp.util.Event
import kotlinx.coroutines.*

class SplashViewModel(
    private val repository: TasksRepository
) : ViewModel() {

    private val TIME_OUT = 4000L

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    private val _triggerNavigation = MutableLiveData<Event<Unit>>()
    val triggerNavigation: LiveData<Event<Unit>> = _triggerNavigation

    private val _doneLoading = MutableLiveData<Boolean>()
    val doneLoading: LiveData<Boolean> get() = _doneLoading

    //this method sync the data to the firestore
    fun syncData() {
        _doneLoading.value = true
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                withTimeoutOrNull(TIME_OUT) {
                    repository.syncTasks()
                }
            }
            delay(2000)
            _doneLoading.value = false
            _triggerNavigation.value = Event(Unit)
        }
    }

}


@Suppress("UNCHECKED_CAST")
class SplashViewModelFactory(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (SplashViewModel(tasksRepository) as T)
}