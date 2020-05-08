package com.alien.brainean.todoapp.data

import androidx.lifecycle.LiveData
/*
    this data source class is going to be accessed be the local and remote data source
 */
interface DataSource {

    suspend fun insertTask(task : Task)

    suspend fun deleteTask(taskId : String)

    suspend fun updateTask(taskId : String, title : String , description : String , isCompleted : Boolean)

    fun observeTasks() : LiveData<Result<List<Task>>>

    suspend fun deleteAllTasks()

    suspend fun deleteCompletedTasks()

    fun observeTask(taskId : String) : LiveData<Result<Task>>

    suspend fun completeTask(taskId : String)

    suspend fun activeTask(taskId: String)

    suspend fun getTask(taskId : String) : Result<Task>

    suspend fun getTasks() : Result<List<Task>>

    suspend fun refreshTasks()
}