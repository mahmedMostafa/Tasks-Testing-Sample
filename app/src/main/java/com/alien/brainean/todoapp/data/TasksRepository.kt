package com.alien.brainean.todoapp.data

import androidx.lifecycle.LiveData

interface TasksRepository {

    fun observeTasks() : LiveData<Result<List<Task>>>

    suspend fun syncTasks()

    suspend fun getTasks(refresh : Boolean = false) : Result<List<Task>>

    suspend fun getTask(taskId : String , refresh: Boolean = false) : Result<Task>

    suspend fun saveTask(task : Task)

    suspend fun deleteTask(task : Task)

    suspend fun deleteAllTasks()

    suspend fun refreshTasks()

    suspend fun completeTask(taskId :String)

    suspend fun activeTask(taskId : String)

    suspend fun deleteCompletedTasks()
}