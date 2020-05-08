package com.alien.brainean.todoapp.data

import androidx.lifecycle.LiveData
import com.alien.brainean.todoapp.data.Result.Error
import com.alien.brainean.todoapp.data.Result.Success

class FakeDataSource(var tasks: MutableList<Task>? = mutableListOf()) : DataSource {

    override suspend fun getTasks(): Result<List<Task>> {
        tasks?.let { return Success(ArrayList(it)) }
        return Error(
            Exception("Tasks not found")
        )
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        //firstOrNull is and inline function that is pretty much like find()
        tasks?.firstOrNull { it.id == taskId }?.let { return Success(it) }
        return Error(
            Exception("Task not found")
        )
    }

    override suspend fun insertTask(task: Task) {
        tasks?.add(task)
    }

    override suspend fun deleteTask(taskId: String) {
        tasks?.removeIf { it.id == taskId }
    }

    override suspend fun deleteAllTasks() {
        tasks?.clear()
    }

    override suspend fun deleteCompletedTasks() {
        tasks?.removeIf { it.isCompleted }
    }


    override suspend fun completeTask(taskId: String) {
        tasks?.firstOrNull { it.id == taskId }?.let { it.isCompleted = true }
    }

    override suspend fun activeTask(taskId: String) {
        tasks?.firstOrNull { it.id == taskId }?.let { it.isCompleted = false }
    }

    override suspend fun refreshTasks() {
        TODO("Not yet implemented")
    }

    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        isCompleted: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        TODO("Not yet implemented")
    }

}