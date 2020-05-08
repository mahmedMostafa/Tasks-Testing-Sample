package com.alien.brainean.todoapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.switchMap
import com.alien.brainean.todoapp.data.local.LocalDataSource
import com.alien.brainean.todoapp.data.remote.RemoteDataSource
import com.alien.brainean.todoapp.util.wrapEspressoIdlingResource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.Exception

class DefaultTasksRepository(
    private val remoteDataSource: DataSource,
    private val localDataSource: DataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TasksRepository {


    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return localDataSource.observeTasks()
    }

    /*
        since remote data source takes some time to get the data so i can't return a success after that b/c it's async
        so i'm implementing here manually
        also something else to notice the cache is going to to upload tasks to the remote not the opposite since the user might not have
        internet while saving the tasks
     */
    override suspend fun syncTasks() {
        wrapEspressoIdlingResource {
            remoteDataSource.getTasks()
            val remoteTasks: MutableList<Task> = mutableListOf()
            val missingTasks: MutableList<Task> = mutableListOf()
            val cacheTasks: List<Task> = ((localDataSource.getTasks()) as Result.Success).data
            //we have to make sure that there is data in the cache to continue first
            if (cacheTasks.size > 1) {
                FirebaseFirestore.getInstance().collection("tasks")
                    .get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val data = task.result?.documents
                            data?.let {
                                for (doc in it) {
                                    val task = doc.toObject(Task::class.java)
                                    remoteTasks.add(task!!)
                                }
                            }
                            //loop through all the cached tasks and see if all are in the remote db
                            for (cachedTask in cacheTasks) {
                                if (!(remoteTasks.contains(cachedTask))) { // the remote tasks don't contain a cached task
                                    missingTasks.add(cachedTask)
                                }
                            }
                            Timber.d("Gemy Successfully synced data")
                        } else {
                            Timber.e("Gemy Failed to sync data ")
                        }
                    }
                //and then we save the missing tasks into the remote data source
                withContext(ioDispatcher) {
                    for (task in missingTasks) {
                        remoteDataSource.insertTask(task)
                    }
                }
            } else {
                Timber.d("I'm here now")
                try {
                    val data =
                        FirebaseFirestore.getInstance().collection("tasks").get().await().documents
                    data.let {
                        for (doc in it) {
                            val task = doc.toObject(Task::class.java)
                            remoteTasks.add(task!!)
                        }
                    }
                    withContext(ioDispatcher) {
                        Timber.d("Adding to local")
                        for (task in remoteTasks) {
                            localDataSource.insertTask(task)
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }

            }
        }
    }

    override suspend fun getTasks(refresh: Boolean): Result<List<Task>> {
        wrapEspressoIdlingResource {
            Timber.d("Gemy getTasks from Repository called")
            if (refresh) {
                try {
                    updateTasksFromRemoteDataSource()
                } catch (e: Exception) {
                    return Result.Error(e)
                }
            }
            return localDataSource.getTasks()
        }
    }

    private suspend fun updateTasksFromRemoteDataSource() {
        wrapEspressoIdlingResource {
            Timber.d("Gemy updateTasksFromRemoteDataSource from Repository called")
            val remoteData = remoteDataSource.getTasks()

            if (remoteData is Result.Success) {
                Timber.d("I'm in result success")
                localDataSource.deleteAllTasks()
                remoteData.data.forEach { task ->
                    localDataSource.insertTask(task)
                }
                Timber.d("Gemy Inserted into database")
                Timber.d("Gemy Data in database  ${localDataSource.getTasks()}")
            } else if (remoteData is Result.Error) {
                Timber.e("Gemy Error")
                throw remoteData.exception
            } else {
                Timber.d("Gemy Loading still")
            }
        }
    }

    private suspend fun updateSingleTaskFromRemoteDataSource(taskId: String) {
        wrapEspressoIdlingResource {
            val remoteData = remoteDataSource.getTask(taskId)

            if (remoteData is Result.Success) {
                localDataSource.insertTask(remoteData.data)
            } else if (remoteData is Result.Error) {
                throw remoteData.exception
            }
        }
    }

    override suspend fun getTask(taskId: String, refresh: Boolean): Result<Task> {
        wrapEspressoIdlingResource {
            if (refresh) {
                try {
                    updateSingleTaskFromRemoteDataSource(taskId)
                } catch (e: Exception) {
                    return Result.Error(e)
                }
            }
            return localDataSource.getTask(taskId)
        }
    }

    override suspend fun saveTask(task: Task) {
        wrapEspressoIdlingResource {
            Timber.d("Gemy DefaultTaskRepository saveTask is called")
            withContext(ioDispatcher) {
                launch { remoteDataSource.insertTask(task) }
                launch { localDataSource.insertTask(task) }
            }
        }
    }

    override suspend fun deleteTask(task: Task) {
        wrapEspressoIdlingResource {
            Timber.d("Delete task called from repository")
            withContext(ioDispatcher) {
                launch { remoteDataSource.deleteTask(task.id) }
                launch { localDataSource.deleteTask(task.id) }
            }
        }
    }

    override suspend fun deleteAllTasks() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                launch { remoteDataSource.deleteAllTasks() }
                launch { localDataSource.deleteAllTasks() }

            }
        }
    }

    override suspend fun refreshTasks() {
        wrapEspressoIdlingResource {
            updateTasksFromRemoteDataSource()
        }
    }

    override suspend fun completeTask(taskId: String) {
        wrapEspressoIdlingResource {
            Timber.d("Complete Task called from repository")
            withContext(ioDispatcher) {
                launch { remoteDataSource.completeTask(taskId) }
                launch { localDataSource.completeTask(taskId) }
            }
        }
    }

    override suspend fun activeTask(taskId: String) {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                launch { remoteDataSource.activeTask(taskId) }
                launch { localDataSource.activeTask(taskId) }
            }
        }
    }

    override suspend fun deleteCompletedTasks() {
        wrapEspressoIdlingResource {
            withContext(ioDispatcher) {
                launch { remoteDataSource.deleteCompletedTasks() }
                launch { localDataSource.deleteCompletedTasks() }
            }
        }
    }
}