package com.alien.brainean.todoapp.util

import android.content.Context
import android.system.Os.close
import androidx.annotation.VisibleForTesting
import com.alien.brainean.todoapp.data.DefaultTasksRepository
import com.alien.brainean.todoapp.data.TasksRepository
import com.alien.brainean.todoapp.data.local.LocalDataSource
import com.alien.brainean.todoapp.data.local.TasksDao
import com.alien.brainean.todoapp.data.local.TasksDatabase
import com.alien.brainean.todoapp.data.remote.RemoteDataSource
import kotlinx.coroutines.runBlocking

object ServiceLocator {

    private val lock = Any()
    private lateinit var tasksDatabase: TasksDatabase

    @Volatile
    var tasksRepository: TasksRepository? = null
        @VisibleForTesting set


    fun provideTasksRepository(context: Context): TasksRepository {
        synchronized(lock) {
            return tasksRepository ?: createRepository(context)
        }
    }

    private fun createRepository(context: Context): TasksRepository {
        tasksDatabase = TasksDatabase.getDatabase(context.applicationContext)
        val newRepository =
            DefaultTasksRepository(RemoteDataSource(), LocalDataSource(tasksDatabase.getTasksDao()))
        tasksRepository = newRepository
        return newRepository
    }

    //TODO take care of this when testing
    @VisibleForTesting
    fun resetRepository(context: Context) {
        synchronized(lock) {
//            runBlocking {
//
//            }
            // Clear all data to avoid test pollution.
            TasksDatabase.getDatabase(context).apply {
                clearAllTables()
                close()
            }
            tasksRepository = null
        }
    }
}