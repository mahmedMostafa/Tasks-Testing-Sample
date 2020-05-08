package com.alien.brainean.todoapp.data.remote

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.alien.brainean.todoapp.data.DataSource
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.util.Firebase
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.Exception

class RemoteDataSource(
) : DataSource {

    private var observableTasks = MutableLiveData<Result<List<Task>>>()
    private var tasks = LinkedHashMap<String, Task>()

    override suspend fun insertTask(task: Task) {
        Firebase.tasksCollection.document(task.id)
            .set(task)
        //.await()
        tasks[task.id] = task
    }

    override suspend fun deleteTask(taskId: String) {
        Firebase.tasksCollection
            .document(taskId)
            .delete()
            .await()
        tasks.remove(taskId)
        Timber.d("Task deleted from firestore")
    }

    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String,
        isCompleted: Boolean
    ) {
        val task =
            Task(id = taskId, title = title, description = description, isCompleted = isCompleted)
        try {
            Firebase.tasksCollection.document(taskId).set(task).await() // maybe i should use update
            tasks[taskId] = task
        } catch (e: Exception) {
            throw Exception("Couldn't update task")
        }
    }

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return observableTasks
    }

    override suspend fun deleteAllTasks() {
        for (task in tasks.values.toList()) {
            Firebase.tasksCollection.document(task.id).delete().await()
        }
        tasks.clear()
    }

    override suspend fun deleteCompletedTasks() {
        for (task in tasks.values.toList()) {
            if (task.isCompleted) {
                Firebase.tasksCollection.document(task.id).delete().await()
                tasks.remove(task.id)
            }
        }
    }

    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        return observableTasks.map { tasks ->
            when (tasks) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(tasks.exception)
                is Result.Success -> {
                    val task = tasks.data.firstOrNull() { it.id == taskId }
                        ?: return@map Result.Error(Exception("Not found"))
                    Result.Success(task)
                }
            }
        }
    }

    override suspend fun completeTask(taskId: String) {
        modifyTaskStatus(taskId, true)
    }

    override suspend fun activeTask(taskId: String) {
        modifyTaskStatus(taskId, false)
    }

    private suspend fun modifyTaskStatus(taskId: String, isCompleted: Boolean) {
        Timber.d("ModifyTaskStatus called from remote data source ${taskId}")
        val task = tasks[taskId]
        task?.let {
            Timber.d("I'm in")
            val completedTask = Task(task.title, task.description, isCompleted, task.id)
            tasks[taskId] = it
            FirebaseFirestore.getInstance().collection("tasks").document(taskId)
                .set(completedTask)
            Timber.d("Task updated to remote data source")
        }
    }

    override suspend fun getTask(taskId: String): Result<Task> {
        return try {
            tasks[taskId]?.let {
                Result.Success(it)
            }
            Result.Error(Exception("Task not found"))
        } catch (e: Exception) {
            Result.Error(Exception("Task not found"))
        }
    }

    override suspend fun getTasks(): Result<List<Task>> {
        Timber.d("Gemy getTasks from remoteDataSource is called")
//        return try {
//            Timber.d("Gemy i'm in")
//            val data = FirebaseFirestore.getInstance().collection("tasks").get().await().documents
//            Timber.d("Gemy Even further")
//            for (doc in data) {
//                val task = doc.toObject<Task>()
//                task?.let {
//                    // tasks.add(it)
//                    tasks[it.id] = it
//                }
//            }
//            Timber.d("Successfully retrieved data from network")
//            Result.Success(tasks.values.toList())
//
//        } catch (e: Exception) {
//            Timber.d("Gemy i'm out")
//            Result.Error(Exception("Tasks failed to load"))
//        }
        var result: Result<List<Task>> = Result.Success(emptyList())
        Timber.d("Gemy been here first")
        FirebaseFirestore.getInstance().collection("tasks").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = task.result?.documents
                    data?.let {
                        for (doc in it) {
                            val task = doc.toObject(Task::class.java)
                            tasks[task!!.id] = task
                        }
                        Timber.d("Gemy Successfully retrieved data from network")
                    }
                    Timber.d("Gemy again success ${tasks.values}")
                    result = Result.Success(tasks.values.toList())
                    observableTasks.value = Result.Success(tasks.values.toList())

                } else {
                    result = Result.Error(Exception(task.exception?.message))
                    observableTasks.value = Result.Error(Exception(task.exception?.message))
                }
            }
        delay(3000)
        return result
    }

    override suspend fun refreshTasks() {
        observableTasks.value = getTasks()
    }
}