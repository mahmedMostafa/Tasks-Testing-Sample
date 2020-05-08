package com.alien.brainean.todoapp.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.alien.brainean.todoapp.MainCoroutineRule
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.data.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

//Integration test so it's a medium test
@MediumTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class LocalDataSourceTest {


    private lateinit var localDataSource: LocalDataSource
    private lateinit var database: TasksDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TasksDatabase::class.java
        ).allowMainThreadQueries().build()
        localDataSource = LocalDataSource(database.getTasksDao(), Dispatchers.Main)
    }

    @After
    fun cleanup() = database.close()


    //Use runBlocking here instead
    @Test
    fun insertTask_getTaskById() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = true)
        localDataSource.insertTask(task)

        //When
        val loaded = localDataSource.getTask(task.id)

        //Then
        assertThat(loaded.succeeded, `is`(true))
        assertThat(loaded, notNullValue())
        loaded as Result.Success
        assertThat(loaded.data.id, `is`(task.id))
        assertThat(loaded.data.title, `is`(task.title))
        assertThat(loaded.data.description, `is`(task.description))
        assertThat(loaded.data.isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun insertSomeTasks_getTasks() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = true)
        val task2 = Task("title", "description", isCompleted = true)
        val task3 = Task("title", "description", isCompleted = true)
        localDataSource.insertTask(task)
        localDataSource.insertTask(task2)
        localDataSource.insertTask(task3)

        //When
        val result = localDataSource.getTasks()

        //Then
        assertThat(result.succeeded, `is`(true))
        assertThat(result, notNullValue())

        result as Result.Success
        assertThat(result.data.size, `is`(3))
    }

    @Test
    fun completeTask_getCompletedTask() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = false)
        localDataSource.insertTask(task)

        //When
        localDataSource.completeTask(task.id)
        val result = localDataSource.getTask(task.id)

        //Then
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.isCompleted, `is`(true))
    }

    @Test
    fun activeTask_getActiveTask() = runBlockingTest {
        //Given
        val task = Task("title", "description", isCompleted = true)
        localDataSource.insertTask(task)

        //When
        localDataSource.activeTask(task.id)
        val result = localDataSource.getTask(task.id)

        //Then
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.isCompleted, `is`(false))
    }

    @Test
    fun cleaCompletedTasks_getAllTasks() = runBlockingTest {
        //Given
        val task1 = Task("title", "description", isCompleted = true)
        val task2 = Task("title", "description", isCompleted = true)
        val task3 = Task("title", "description", isCompleted = false)
        localDataSource.insertTask(task1)
        localDataSource.insertTask(task2)
        localDataSource.insertTask(task3)

        //When
        localDataSource.deleteCompletedTasks()
        val result = localDataSource.getTasks()

        //Then
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(localDataSource.getTask(task1.id).succeeded, `is`(false))
        assertThat(localDataSource.getTask(task2.id).succeeded, `is`(false))
        assertThat(localDataSource.getTask(task3.id).succeeded, `is`(true))
        assertThat(result.data.size, `is`(1))
        assertThat(result.data[0], `is`(task3))
    }

    @Test
    fun deleteAllTasks_getAllTasks() = runBlockingTest {
        //Given
        val task1 = Task("title", "description", isCompleted = true)
        val task2 = Task("title", "description", isCompleted = true)
        localDataSource.insertTask(task1)
        localDataSource.insertTask(task2)

        //When
        localDataSource.deleteAllTasks()
        val result = localDataSource.getTasks() as Result.Success

        //Then
        assertThat(result.data.isEmpty(), `is`(true))
    }
}