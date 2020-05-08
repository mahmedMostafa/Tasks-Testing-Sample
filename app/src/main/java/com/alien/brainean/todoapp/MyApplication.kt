package com.alien.brainean.todoapp

import android.app.Application
import com.alien.brainean.todoapp.data.TasksRepository
import com.alien.brainean.todoapp.util.ServiceLocator
import timber.log.Timber

class MyApplication : Application() {

    val taskRepository: TasksRepository
        get() = ServiceLocator.provideTasksRepository(this)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}