package com.alien.brainean.todoapp.detail

import androidx.lifecycle.*
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.data.TasksRepository
import com.alien.brainean.todoapp.util.Event
import kotlinx.coroutines.launch
import timber.log.Timber

class DetailViewModel(private val repository: TasksRepository) : ViewModel() {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    private val _triggerNavigation = MutableLiveData<Event<Unit>>()

    val triggerNavigation: LiveData<Event<Unit>>
        get() = _triggerNavigation

    private val _snackbarText = MutableLiveData<Event<String>>()
    val snackbarText: LiveData<Event<String>> = _snackbarText

    var taskId: String? = null

    var isNewTask: Boolean = false
    var taskCompleted = false

    fun saveTask() {
        Timber.d("Gemy saveTask is called ${title.value}")
        val currentTitle = title.value
        val currentDescription = description.value

        if (currentTitle == null || currentTitle.isEmpty()) {
            Timber.d("Gemy first")
            _snackbarText.value = Event("Title can't be empty")
            return
        } else if (currentDescription == null || currentDescription.isEmpty()) {
            Timber.d("Gemy second")
            _snackbarText.value = Event("Description can't be empty")
            return
        }
        val currentTaskId = taskId
        if (isNewTask) {
            Timber.d("Gemy third")
            createTask(Task(currentTitle, currentDescription))
        } else {
            Timber.d("Gemy forth")
            val task = Task(currentTitle, currentDescription, taskCompleted, currentTaskId!!)
            updateTask(task)
        }
    }

    fun start(taskId: String) {
        Timber.d("Gemy start is called")
        this.taskId = taskId
        if (taskId == "new") {
            isNewTask = true
            return
        }

        isNewTask = false

        viewModelScope.launch {
            if(taskId != "new"){
                repository.getTask(taskId).let { result ->
                    if (result is Result.Success) {
                        title.value = result.data.title
                        description.value = result.data.description
                        taskCompleted = result.data.isCompleted
                    }
                }
            }
        }

    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        repository.saveTask(task)
        _triggerNavigation.value = Event(Unit)
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        Timber.d("Gemy createTask is called")
        repository.saveTask(task)
        _triggerNavigation.value = Event(Unit)
    }
}

@Suppress("UNCHECKED_CAST")
class DetailViewModelFactory(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (DetailViewModel(tasksRepository) as T)
}