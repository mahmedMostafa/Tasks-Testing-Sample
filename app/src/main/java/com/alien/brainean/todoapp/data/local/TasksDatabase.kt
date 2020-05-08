package com.alien.brainean.todoapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alien.brainean.todoapp.data.Task

@Database(entities = [Task::class], version = 1)
abstract class TasksDatabase : RoomDatabase() {

    abstract fun getTasksDao(): TasksDao

    companion object {
        @Volatile
        private var instance: TasksDatabase? = null
        private val LOCK = Any()

        fun getDatabase(context: Context): TasksDatabase {
            return instance ?: buildDatabase(context.applicationContext)
        }

        private fun buildDatabase(context: Context): TasksDatabase {
            return synchronized(LOCK) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TasksDatabase::class.java,
                    "tasks.db"
                )
                    .build()
            }
        }
    }
}