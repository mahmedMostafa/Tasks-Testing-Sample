package com.alien.brainean.todoapp.tasks

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.alien.brainean.todoapp.FakeRepository
import com.alien.brainean.todoapp.R
import com.alien.brainean.todoapp.data.Result
import com.alien.brainean.todoapp.data.Task
import com.alien.brainean.todoapp.data.TasksRepository
import com.alien.brainean.todoapp.util.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@MediumTest
@ExperimentalCoroutinesApi
class TasksFragmentTest {

    private lateinit var tasksRepository: TasksRepository

    @Before
    fun initRepository() {
        tasksRepository = FakeRepository()
        ServiceLocator.tasksRepository = tasksRepository
    }

    @After
    fun cleanup() {
        ServiceLocator.resetRepository()
    }

    @Test
    fun saveTasks_displayedInFragment() = runBlockingTest {
        //Given
        tasksRepository.saveTask(Task("Title1", "description1", isCompleted = false, id = "id1"))
        tasksRepository.saveTask(Task("Title2", "description2", isCompleted = false, id = "id2"))

        //When
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        //Then
        onView(withText("Title1")).check(matches(isDisplayed()))
        onView(withText("description2")).check(matches(isDisplayed()))
    }

    @Test
    fun noTasksSaved_noTasksDisplayedIntFragment() = runBlockingTest {
        //Given
        tasksRepository.deleteAllTasks()

        //When
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        //Then
        onView(withId(R.id.empty_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun completedTasksOnly_noTasksDisplayedInActiveChip() = runBlockingTest {
        //Given
        tasksRepository.saveTask(Task("Title1", "description1", isCompleted = true, id = "id1"))
        tasksRepository.saveTask(Task("Title2", "description2", isCompleted = true, id = "id2"))

        //When
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        //Then
        onView(withText("Title1")).check(matches(isDisplayed()))
        onView(withText("Title2")).check(matches(isDisplayed()))
        onView(withId(R.id.empty_layout)).check(matches(not(isDisplayed())))
        onView(withId(R.id.active_tasks_chip)).perform(click())
        onView(withId(R.id.empty_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.completed_tasks_chip)).perform(click())
        onView(withId(R.id.empty_layout)).check(matches(not(isDisplayed())))
        onView(withText("description1")).check(matches(isDisplayed()))
        onView(withText("description2")).check(matches(isDisplayed()))
    }

    @Test
    fun clickFab_navigateToDetailFragment() {
        //Given
        val navController = mock(NavController::class.java)
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        onView(withId(R.id.add_task_fab)).perform(click())

        //Then
        verify(navController).navigate(
            TasksFragmentDirections.actionTasksFragmentToDetailFragment("new")
        )
    }

    @Test
    fun clickOnTaskItem_navigateToDetailFragment() = runBlockingTest {
        //Given
        tasksRepository.saveTask(Task("Title1", "description1", isCompleted = true, id = "id1"))
        tasksRepository.saveTask(Task("Title2", "description2", isCompleted = true, id = "id2"))
        val navController = mock(NavController::class.java)
        val scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When
        onView(withId(R.id.recycler_view))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Title1")), click()
                )
            )

        //Then
        verify(navController).navigate(
            TasksFragmentDirections.actionTasksFragmentToDetailFragment("id1")
        )
    }

    @Test
    fun swipeItem_checkDeleted() = runBlockingTest {
        //Given
        val task1 = Task("Title1", "description1", isCompleted = true, id = "id1")
        val task2 = Task("Title2", "description2", isCompleted = true, id = "id2")
        tasksRepository.saveTask(task1)
        tasksRepository.saveTask(task2)
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        assertThat((tasksRepository.getTasks() as Result.Success).data.contains(task1), `is`(true))

        //When
        onView(withId(R.id.recycler_view))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    GeneralSwipeAction(
                        Swipe.FAST, GeneralLocation.BOTTOM_LEFT, GeneralLocation.BOTTOM_RIGHT,
                        Press.FINGER
                    )
                )
            )

        //Then
        assertThat((tasksRepository.getTasks() as Result.Success).data.contains(task1), `is`(false))
    }
}