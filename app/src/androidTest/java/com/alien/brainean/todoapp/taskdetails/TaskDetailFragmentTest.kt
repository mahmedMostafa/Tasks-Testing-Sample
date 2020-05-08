package com.alien.brainean.todoapp.taskdetails

import android.os.Bundle
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.alien.brainean.todoapp.FakeRepository
import com.alien.brainean.todoapp.R
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.TasksRepository
import com.alien.brainean.todoapp.detail.DetailFragment
import com.alien.brainean.todoapp.detail.DetailFragmentArgs
import com.alien.brainean.todoapp.detail.DetailFragmentDirections
import com.alien.brainean.todoapp.util.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class TaskDetailFragmentTest {

    private lateinit var tasksRepository: TasksRepository

    @Before
    fun initTasksRepository() {
        tasksRepository = FakeRepository()
        ServiceLocator.tasksRepository = tasksRepository
    }

    @After
    fun cleanup() {
        ServiceLocator.resetRepository()
    }

    @Test
    fun emptyTask_noSaved() {
        //Given
        val bundle = DetailFragmentArgs("new").toBundle()
        launchFragmentInContainer<DetailFragment>(bundle, R.style.AppTheme)
        onView(withId(R.id.title_edit_Text)).check(matches(isDisplayed()))
        onView(withId(R.id.description_edit_text)).check(matches(isDisplayed()))

        //When
        onView(withId(R.id.title_text_input)).perform(clearText())
        onView(withId(R.id.description_text_input)).perform(clearText())
        onView(withId(R.id.done_button)).perform(click())

        //Then
        onView(withId(R.id.title_edit_Text)).check(matches(isDisplayed()))
    }

    @Test
    fun insertTaskRight_navigatedToTasksFragment() {
        //Given
        val navController = mock(NavController::class.java)
        val bundle = DetailFragmentArgs("new").toBundle()
        val scenario = launchFragmentInContainer<DetailFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        onView(withId(R.id.title_text_input)).perform(replaceText("title"))
        onView(withId(R.id.description_text_input)).perform(replaceText("description"))
        onView(withId(R.id.done_button)).perform(click())

        //Then
        verify(navController).navigate(
            DetailFragmentDirections.actionDetailFragmentToTasksFragment()
        )

    }

    @Test
    fun insertTask_checkInsertedTaskInRepository() = runBlockingTest{
        //Given
        val navController = mock(NavController::class.java)
        val bundle = DetailFragmentArgs("new").toBundle()
        val scenario = launchFragmentInContainer<DetailFragment>(bundle, R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //
        onView(withId(R.id.title_text_input)).perform(replaceText("title120"))
        onView(withId(R.id.description_text_input)).perform(replaceText("description"))
        onView(withId(R.id.done_button)).perform(click())

        //Then
        val tasks = (tasksRepository.getTasks(false) as Result.Success).data
        assertThat(tasks[0].title,`is`("title120"))
        assertThat(tasks[0].description,`is`("description"))
    }

}