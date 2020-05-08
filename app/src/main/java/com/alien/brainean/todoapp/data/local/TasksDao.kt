package com.alien.brainean.todoapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.alien.brainean.todoapp.data.Task

@Dao
interface TasksDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM tasks")
    suspend fun getTasks() : List<Task>

    @Query("SELECT * FROM tasks")
    fun observeTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeTask(taskId: String): LiveData<Task>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTask(taskId: String): Task

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateCompleted(taskId: String, isCompleted: Boolean)

    @Update
    suspend fun updateTask(task: Task): Int //this is going to return 1 if succeeded
}