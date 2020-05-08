package com.alien.brainean.todoapp.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.alien.brainean.todoapp.MainCoroutineRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class DefaultTasksRepositoryTest {

    private lateinit var tasksRepository: DefaultTasksRepository
    private val task1 = Task("title1", "description1", isCompleted = false)
    private val task2 = Task("title2", "description2", isCompleted = true)
    private val task3 = Task("title3", "description3", isCompleted = true)
    private val task4 = Task("title4", "description4", isCompleted = false)
    private val newTask = Task("newTitle", "newDescription", isCompleted = false)
    private val remoteTasks = listOf(task1, task2).sortedBy { it.id }
    private val localTasks = listOf(task3, task4).sortedBy { it.id }
    private val newTasks = listOf(newTask).sortedBy { it.id }
    private lateinit var localDataSource: FakeDataSource
    private lateinit var remoteDataSource: FakeDataSource

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initRepository() {
        remoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        localDataSource = FakeDataSource(localTasks.toMutableList())
        tasksRepository =
            DefaultTasksRepository(remoteDataSource, localDataSource, Dispatchers.Main)
    }


    @Test
    fun getTasks_requestAllTasksFromRemoteDataSource() = mainCoroutineRule.runBlockingTest {
        //When
        val tasks = tasksRepository.getTasks(true) as Result.Success

        //Then
        assertThat(tasks.data, `is`(remoteTasks))
    }

    @Test
    fun getTasks_remoteTasksEqualsLocalTasks() = mainCoroutineRule.runBlockingTest {
        //When
        val tasks = tasksRepository.getTasks(false) as Result.Success

        //Then
        assertThat(tasks.data, `is`(localTasks))
    }

    @Test
    fun saveTask_checkForRemoteAndLocalTasks() = mainCoroutineRule.runBlockingTest {
        //Given that it's not in neither in the local or the remote data sources
        assertThat(remoteDataSource.tasks?.contains(newTask), `is`(false))
        assertThat(localDataSource.tasks?.contains(newTask), `is`(false))

        //When
        tasksRepository.saveTask(newTask)

        //Then
        assertThat(remoteDataSource.tasks?.contains(newTask), `is`(true))
        assertThat(localDataSource.tasks?.contains(newTask), `is`(true))
    }

    @Test
    fun completeAndActiveTask_retrieveTask() = mainCoroutineRule.runBlockingTest {
        //Given
        tasksRepository.saveTask(newTask)

        //When
        tasksRepository.completeTask(newTask.id)

        //Then
        assertThat(
            (tasksRepository.getTask(newTask.id) as Result.Success).data.isCompleted,
            `is`(true)
        )

        //When
        tasksRepository.activeTask(newTask.id)

        //Then
        assertThat(
            (tasksRepository.getTask(newTask.id) as Result.Success).data.isCompleted,
            `is`(false)
        )
    }

    @Test
    fun clearCompletedTasks_getTasksFromRemoteAndLocal() = mainCoroutineRule.runBlockingTest {
        //Given that the sized are 2 for each first
        assertThat(localDataSource.tasks?.size, `is`(2))
        assertThat(remoteDataSource.tasks?.size, `is`(2))

        //When
        tasksRepository.deleteCompletedTasks()

        //Then
        assertThat(localDataSource.tasks?.size, `is`(1))
        assertThat(remoteDataSource.tasks?.size, `is`(1))
    }

    @Test
    fun deleteAllTasks_getAllTasks() = mainCoroutineRule.runBlockingTest {
        val initialData = (tasksRepository.getTasks(false) as Result.Success).data

        //When
        tasksRepository.deleteAllTasks()

        //Then
        assertThat((tasksRepository.getTasks() as Result.Success).data.isEmpty(), `is`(true))
        assertThat(initialData.isEmpty(), `is`(false))
        assertThat(initialData.size, `is`(2))
    }

    @Test
    fun deleteSingleTask_retriveTasksSize() = mainCoroutineRule.runBlockingTest {
        val initialSize = (tasksRepository.getTasks(true) as Result.Success).data.size

        //When
        tasksRepository.deleteTask(task1)

        val loadedSize = (tasksRepository.getTasks(true) as Result.Success).data.size

        //Then
        assertThat(loadedSize, `is`(initialSize - 1))
        assertThat(loadedSize, `is`(1))
        assertThat(
            (tasksRepository.getTasks(true) as Result.Success).data.contains(task1),
            `is`(false)
        )
    }
}