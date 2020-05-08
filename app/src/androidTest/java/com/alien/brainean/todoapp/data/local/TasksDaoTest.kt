package com.alien.brainean.todoapp.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.alien.brainean.todoapp.data.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@SmallTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TasksDaoTest {

    private lateinit var database: TasksDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TasksDatabase::class.java
        ).build()
    }

    @After
    fun cleanupDb() = database.close()


    @Test
    fun insertTask_getTaskById() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = false)
        database.getTasksDao().insertTask(task)

        //When
        val loadedTask = database.getTasksDao().getTask(task.id)

        //Then
        assertThat(loadedTask, notNullValue())
        assertThat(loadedTask.id, `is`(task.id))
        assertThat(loadedTask.title, `is`(task.title))
        assertThat(loadedTask.description, `is`(task.description))
        assertThat(loadedTask.isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun insertTask_checkListContainsTask() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = false)
        database.getTasksDao().insertTask(task)

        //When
        val tasks = database.getTasksDao().getTasks()

        //Then
        assertThat(tasks.isEmpty(), `is`(false))
        assertThat(tasks, notNullValue())
        assertThat(tasks.contains(task), `is`(true))
        assertThat(tasks[0].title, `is`(task.title))
        assertThat(tasks.size, `is`(1))
    }

    @Test
    fun insertTask_replaceOnConflict() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = false)
        database.getTasksDao().insertTask(task)

        //When
        val updatedTask = Task("newTitle", "newDescription", isCompleted = true, id = task.id)
        database.getTasksDao().insertTask(updatedTask)

        //Then
        val loaded = database.getTasksDao().getTask(task.id)
        assertThat(database.getTasksDao().getTasks().size, `is`(1))
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.title, `is`("newTitle"))
        assertThat(loaded.description, `is`("newDescription"))
        assertThat(loaded.isCompleted, `is`(true))
    }


    @Test
    fun insertTask_updateTask_getTaskById() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = false)
        database.getTasksDao().insertTask(task)

        //When
        val updatedTask = Task("newTitle", "newDescription", isCompleted = true, id = task.id)
        database.getTasksDao().updateTask(updatedTask)

        //Then
        val loaded = database.getTasksDao().getTask(task.id)
        assertThat(database.getTasksDao().getTasks().size, `is`(1))
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.title, `is`("newTitle"))
        assertThat(loaded.description, `is`("newDescription"))
        assertThat(loaded.isCompleted, `is`(true))
    }

    @Test
    fun updateCompletedTask_getTaskById() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = false)
        database.getTasksDao().insertTask(task)

        //When
        database.getTasksDao().updateCompleted(task.id, true)

        //Then
        val loaded = database.getTasksDao().getTask(task.id)
        assertThat(loaded.isCompleted, `is`(true))
    }

    @Test
    fun deleteTask_getAllTasks() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = false)
        database.getTasksDao().insertTask(task)

        //When
        database.getTasksDao().deleteTask(task.id)

        //Then
        val tasks = database.getTasksDao().getTasks()
        assertThat(tasks.isEmpty(), `is`(true))
    }

    @Test
    fun deleteAllTask_getAllTasks() = runBlockingTest {
        //Given
        val task1 = Task("title", "description", isCompleted = false)
        val task2 = Task("title", "description", isCompleted = false)
        val task3 = Task("title", "description", isCompleted = false)
        database.getTasksDao().insertTask(task1)
        database.getTasksDao().insertTask(task2)
        database.getTasksDao().insertTask(task3)

        //When
        database.getTasksDao().deleteAllTasks()

        //Then
        val tasks = database.getTasksDao().getTasks()
        assertThat(tasks.isEmpty(),`is`(true))
    }

    @Test
    fun deleteCompletedTasks_getTasks() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = true)
        database.getTasksDao().insertTask(task)

        //When
        database.getTasksDao().deleteTask(task.id)

        //Then
        val tasks = database.getTasksDao().getTasks()
        assertThat(tasks.isEmpty(),`is`(true))
    }
}