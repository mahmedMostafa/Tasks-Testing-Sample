package com.alien.brainean.todoapp.tasks

import androidx.lifecycle.*
import com.alien.brainean.todoapp.data.DefaultTasksRepository
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.data.TasksRepository
import com.alien.brainean.todoapp.util.Event
import kotlinx.coroutines.launch
import timber.log.Timber

class TasksViewModel(private val repository: TasksRepository) : ViewModel() {

    private var currentFiltering = TasksFilterType.ALL_TASKS

    private val _update = MutableLiveData<Boolean>(false)
    val update: MutableLiveData<Boolean>
        get() = _update

    private val _triggerNavigation = MutableLiveData<Event<String>>()
    val triggerNavigation: LiveData<Event<String>> get() = _triggerNavigation

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> get() = _snackbarMessage

    //called by data binding
    fun triggerNav(taskId: String) {
        _triggerNavigation.value = Event(taskId)
    }

    private val _tasks: LiveData<List<Task>> = Transformations.switchMap(_update) { update ->
        Timber.d("Gemy Switchmap update is triggered")
//        if (update) {
////            viewModelScope.launch {
////                repository.refreshTasks()
////            }
//        }
        repository.observeTasks().switchMap { filterTasks(it) }
    }

    init {
        Timber.d("Gemy ViewModel constructed well")
        loadTasks(true)
    }

    //doesn't work properly by observing in xml idk why
    val empty: LiveData<Boolean> = Transformations.map(_tasks) {
        it.isEmpty()
    }

    private fun filterTasks(tasksResult: Result<List<Task>>): LiveData<List<Task>> {
        val result = MutableLiveData<List<Task>>()
        if (tasksResult is Result.Success) {
            viewModelScope.launch {
                result.value = filterItems(tasksResult.data, currentFiltering)
            }
        } else {
            result.value = emptyList()
            //Todo loading or error
        }
        return result
    }

    fun setFilter(filteringType: TasksFilterType) {
        currentFiltering = filteringType
        loadTasks(true)
    }

    val tasks: LiveData<List<Task>> = _tasks

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
        _snackbarMessage.value = Event("Task deleted")
    }

    fun changeTaskStatus(task: Task, completed: Boolean) = viewModelScope.launch {
        if (completed) {
            repository.completeTask(task.id)
            _snackbarMessage.value = Event("Task is Completed")
        } else {
            repository.activeTask(task.id)
            _snackbarMessage.value = Event("Task is Active")
        }
    }

    private fun filterItems(tasks: List<Task>, filteringType: TasksFilterType): List<Task> {
        val tasksToShow = ArrayList<Task>()
        // We filter the tasks based on the requestType
        for (task in tasks) {
            when (filteringType) {
                TasksFilterType.ALL_TASKS -> tasksToShow.add(task)
                TasksFilterType.ACTIVE_TASKS -> if (task.isActive) {
                    tasksToShow.add(task)
                }
                TasksFilterType.COMPLETED_TASKS -> if (task.isCompleted) {
                    tasksToShow.add(task)
                }
            }
        }
        return tasksToShow
    }

    fun deleteAllTasks() = viewModelScope.launch {
        repository.deleteAllTasks()
    }

    fun clearCompletedTasks() = viewModelScope.launch {
        repository.deleteCompletedTasks()
    }

    //when this is set to true the tasks gets loaded
    fun loadTasks(update: Boolean) {
        _update.value = update
    }

}

@Suppress("UNCHECKED_CAST")
class TasksViewModelFactory(
    private val tasksRepository: TasksRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>) =
        (TasksViewModel(tasksRepository) as T)
}