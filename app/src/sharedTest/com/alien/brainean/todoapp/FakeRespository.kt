package com.alien.brainean.todoapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.data.TasksRepository
import kotlinx.coroutines.runBlocking
import java.util.LinkedHashMap


//this is a fake implementation of the repository
class FakeRepository : TasksRepository {

    var tasksServiceData: LinkedHashMap<String, Task> = LinkedHashMap()

    private var shouldReturnError = false

    private val observableTasks = MutableLiveData<Result<List<Task>>>()

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    //runBlocking is only used for fake classes and runBlockingTest is for testable classes
    override fun observeTasks(): LiveData<Result<List<Task>>> {
        runBlocking { refreshTasks() }
        return observableTasks
    }

    override suspend fun syncTasks() {
        TODO("Not yet implemented")
    }

    override suspend fun getTasks(refresh: Boolean): Result<List<Task>> {
        if (shouldReturnError) {
            return Result.Error(Exception("Test exception"))
        }
        return Result.Success(tasksServiceData.values.toList())
    }

    override suspend fun getTask(taskId: String, refresh: Boolean): Result<Task> {
        if (shouldReturnError) {
            return Result.Error(Exception("Test exception"))
        }
        tasksServiceData[taskId]?.let {
            return Result.Success(it)
        }
        return Result.Error(Exception("Could not find task"))
    }

    override suspend fun saveTask(task: Task) {
        tasksServiceData[task.id] = task
    }

    override suspend fun deleteTask(task: Task) {
        tasksServiceData.remove(task.id)
        refreshTasks()
    }

    override suspend fun deleteAllTasks() {
        tasksServiceData.clear()
       // refreshTasks()
    }

    override suspend fun refreshTasks() {
        observableTasks.value = getTasks()
    }

    override suspend fun completeTask(taskId: String) {
        val task = tasksServiceData[taskId]
        tasksServiceData[taskId] = task!!.copy(isCompleted = true)
        refreshTasks()
    }

    override suspend fun activeTask(taskId: String) {
        val task = tasksServiceData[taskId]
        tasksServiceData[taskId] = task!!.copy(isCompleted = false)
        refreshTasks()
    }

    override suspend fun deleteCompletedTasks() {
        for (task in tasksServiceData.values.toList()) {
            if (task.isCompleted) {
                tasksServiceData.remove(task.id)
            }
        }
    }

    public fun addTasks(vararg tasks: Task) {
        for (task in tasks) {
            tasksServiceData[task.id] = task
        }
        runBlocking { refreshTasks() }
    }

}