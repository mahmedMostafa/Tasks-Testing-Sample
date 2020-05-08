package com.alien.brainean.todoapp.data.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.alien.brainean.todoapp.data.DataSource
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.Task
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception

class LocalDataSource(
    private val tasksDao: TasksDao,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DataSource {

    override suspend fun insertTask(task: Task) = withContext(coroutineDispatcher) {
        Timber.d("Gemy InsertTask called")
        tasksDao.insertTask(task)
    }

    override suspend fun deleteTask(taskId: String) = withContext(coroutineDispatcher) {
        tasksDao.deleteTask(taskId)
        Timber.d("Task deleted from room")
    }

    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        isCompleted: Boolean
    ) = withContext(coroutineDispatcher){
       // tasksDao.updateTask(Task(id = taskId , title = title , description = description , isCompleted = isCompleted))
    }

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return tasksDao.observeTasks().map {
            Result.Success(it)
        }
    }


    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        return tasksDao.observeTask(taskId).map {
            Result.Success(it)
        }
    }

    override suspend fun deleteAllTasks() = withContext(coroutineDispatcher) {
        Timber.d("Gemy DeleteAllTasks called")
        tasksDao.deleteAllTasks()
    }

    override suspend fun deleteCompletedTasks() = withContext(coroutineDispatcher) {
        tasksDao.deleteCompletedTasks()
    }

    override suspend fun completeTask(taskId: String) = withContext(coroutineDispatcher) {
        tasksDao.updateCompleted(taskId, true)
        Timber.d("Task updated from local data source")
    }

    override suspend fun activeTask(taskId: String) {
        tasksDao.updateCompleted(taskId, false)
    }

    override suspend fun getTask(taskId: String): Result<Task> = withContext(coroutineDispatcher) {
        try {
            val task = tasksDao.getTask(taskId)
            if (task != null) {
                return@withContext Result.Success(task)
            } else {
                return@withContext Result.Error(Exception("Task not found"))
            }
        } catch (e: Exception) {
            return@withContext Result.Error(e)
        }
    }

    override suspend fun getTasks(): Result<List<Task>> = withContext(coroutineDispatcher) {
        return@withContext try {
            Result.Success(tasksDao.getTasks())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun refreshTasks() {
        TODO("Not yet implemented")
    }
}