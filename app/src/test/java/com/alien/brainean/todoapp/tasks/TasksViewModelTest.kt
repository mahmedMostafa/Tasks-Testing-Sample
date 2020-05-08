package com.alien.brainean.todoapp.tasks

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.alien.brainean.todoapp.FakeRepository
import com.alien.brainean.todoapp.MainCoroutineRule
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class TasksViewModelTest {

    //class under test
    private lateinit var viewModel: TasksViewModel

    private lateinit var fakeRepository: FakeRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initViewModel() {
        fakeRepository = FakeRepository()
        val task1 = Task("Title1", "Description1")
        val task2 = Task("Title2", "Description2", true)
        val task3 = Task("Title3", "Description3", true)
        fakeRepository.addTasks(task1, task2, task3)

        viewModel = TasksViewModel(fakeRepository)
    }

    @Test
    fun completeTask_snackbarMessageUpdated() {
        //Given
        val task = Task("title", "description")
        fakeRepository.addTasks(task)

        //When
        viewModel.changeTaskStatus(task, true)

        //Then
        assertThat(fakeRepository.tasksServiceData[task.id]?.isCompleted, `is`(true))
        val snackbarText = viewModel.snackbarMessage.getOrAwaitValue()
        assertThat(snackbarText.getContentIfNotHandled(), `is`("Task is Completed"))
    }

    @Test
    fun activeTask_snackbarMessageUpdated() {
        //Given
        val task = Task("title", "description")
        fakeRepository.addTasks(task)

        //When
        viewModel.changeTaskStatus(task, false)

        //Then
        assertThat(fakeRepository.tasksServiceData[task.id]?.isCompleted, `is`(false))
        val snackbarText = viewModel.snackbarMessage.getOrAwaitValue()
        assertThat(snackbarText.getContentIfNotHandled(), `is`("Task is Active"))
    }

    @Test
    fun addTask_triggerNavigationUpdated() {
        //Given
        val task = Task("title", "description")
        fakeRepository.addTasks(task)

        //When
        viewModel.triggerNav(task.id)

        //Then
        val value = viewModel.triggerNavigation.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled(), `is`(notNullValue()))
    }

    @Test
    fun initViewModel_loadTasks() {

        //Given viewModel is initialized

        //When
        viewModel.loadTasks(true)

        //Then
        val value = viewModel.update.getOrAwaitValue()
        assertThat(value, `is`(true))
        val tasks = viewModel.tasks.getOrAwaitValue()
        assertThat(tasks.size, `is`(3))
    }

    @Test
    fun deleteTask_getAllTasks() {

        //Given
        val task = Task("Title", "Description")
        fakeRepository.addTasks(task)

        //When
        viewModel.deleteTask(task)

        //Then
        val value = viewModel.snackbarMessage.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled(), `is`("Task deleted"))
        assertThat(fakeRepository.tasksServiceData.size, `is`(3))
        assertThat(fakeRepository.tasksServiceData[task.id], `is`(nullValue()))
    }

    @Test
    fun clearCompletedTasks_getAllTasks() {
        //Given (two completed and two active tasks)
        val task = Task("title", "description", isCompleted = true)
        fakeRepository.addTasks(task)

        //When
        viewModel.clearCompletedTasks()

        //Then
        assertThat(fakeRepository.tasksServiceData.size, `is`(1))
        assertThat(fakeRepository.tasksServiceData[task.id], `is`(nullValue()))
    }

    @Test
    fun filterCompletedTasks_getAllTasks() {
        //Given viewModel is initialized

        //When
        viewModel.setFilter(TasksFilterType.COMPLETED_TASKS)

        //Then
        val tasks = viewModel.tasks.getOrAwaitValue()
        assertThat(tasks.size, `is`(2))
    }

    @Test
    fun filterActiveTasks_getAllTasks() {
        //Given viewModel is initialized

        //When
        viewModel.setFilter(TasksFilterType.ACTIVE_TASKS)

        //Then
        val tasks = viewModel.tasks.getOrAwaitValue()
        assertThat(tasks.size, `is`(1))
    }
}