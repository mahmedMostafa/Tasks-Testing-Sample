package com.alien.brainean.todoapp.detail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.alien.brainean.todoapp.FakeRepository
import com.alien.brainean.todoapp.MainCoroutineRule
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class DetailViewModelTest {

    private lateinit var fakeRepository: FakeRepository

    private lateinit var viewModel: DetailViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        fakeRepository = FakeRepository()
        viewModel = DetailViewModel(fakeRepository)
    }

    @Test
    fun saveTask_getTaskFromRepository() {
        //Given
        val title = "title"
        val description = "description"
        viewModel.apply {
            this.title.value = title
            this.description.value = description
            isNewTask = true
        }

        //When
        viewModel.saveTask()

        //Then
        assertThat(fakeRepository.tasksServiceData.values.first().title, `is`(title))
        assertThat(fakeRepository.tasksServiceData.values.first().description, `is`(description))
    }

    @Test
    fun saveTaskWithNullTitle_triggerSnackbarMessage() {
        //Given
        val description = "description"
        viewModel.apply {
            this.title.value = null
            this.description.value = description
            isNewTask = true
        }

        //When
        viewModel.saveTask()

        //Then
        val value = viewModel.snackbarText.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled(), `is`("Title can't be empty"))
    }

    @Test
    fun saveTaskWithNullDescription_triggerSnackbarMessage() {
        //Given
        val title = "title"
        viewModel.apply {
            this.title.value = title
            this.description.value = null
            isNewTask = true
        }

        //When
        viewModel.saveTask()

        //Then
        val value = viewModel.snackbarText.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled(), `is`("Description can't be empty"))
    }

    @Test
    fun saveTask_triggerNavigation() {
        //Given
        val title = "title"
        val description = "description"
        viewModel.apply {
            this.title.value = title
            this.description.value = description
            isNewTask = true
        }

        //When
        viewModel.saveTask()

        //Then
        val value = viewModel.triggerNavigation.getOrAwaitValue()
        assertThat(value.getContentIfNotHandled(), `is`(notNullValue()))
    }

    @Test
    fun updateTaskId_replaceTaskInRepository() {
        //Given
        val title = "newTitle"
        val description = "newDescription"
        fakeRepository.addTasks(Task("title", "description", isCompleted = false, id = "mohamed"))
        viewModel.apply {
            this.title.value = title
            this.description.value = description
            isNewTask = false
            taskId = "mohamed"
        }

        //When
        viewModel.saveTask()

        //Then
        assertThat(fakeRepository.tasksServiceData.values.first().title, `is`("newTitle"))
        assertThat(
            viewModel.triggerNavigation.getOrAwaitValue().getContentIfNotHandled(), `is`(
                notNullValue()
            )
        )
    }
}