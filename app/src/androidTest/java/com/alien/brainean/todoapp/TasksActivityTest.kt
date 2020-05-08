package com.alien.brainean.todoapp

//i tried to make end to end tests but it didn't work for an issue with the idiling resource dependency that i couldn't solve
//import androidx.test.core.app.ActivityScenario
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.espresso.Espresso.onView
//import androidx.test.espresso.Espresso.pressBack
//import androidx.test.espresso.IdlingRegistry
//import androidx.test.espresso.action.ViewActions.click
//import androidx.test.espresso.assertion.ViewAssertions.matches
//import androidx.test.espresso.matcher.ViewMatchers.*
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.LargeTest
//import com.alien.brainean.todoapp.data.Task
//import com.alien.brainean.todoapp.data.TasksRepository
//import com.alien.brainean.todoapp.ui.MainActivity
//import com.alien.brainean.todoapp.util.DataBindingIdlingResource
//import com.alien.brainean.todoapp.util.EspressoIdlingResource
//import com.alien.brainean.todoapp.util.ServiceLocator
//import com.alien.brainean.todoapp.util.monitorActivity
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.test.runBlockingTest
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//
//
//@LargeTest
//@RunWith(AndroidJUnit4::class)
//@ExperimentalCoroutinesApi
//class TasksActivityTest {
//
//        private lateinit var tasksRepository: TasksRepository
//
//        // An Idling Resource that waits for Data Binding to have no pending bindings
//        private val dataBindingIdlingResource = DataBindingIdlingResource()
//
//        @Before
//        fun init() {
//            tasksRepository =
//                ServiceLocator.provideTasksRepository(ApplicationProvider.getApplicationContext())
//        }
//
//        @After
//        fun resetRepository() {
//            ServiceLocator.resetRepository(ApplicationProvider.getApplicationContext())
//        }
//
//        @Before
//        fun registerIdelingResourse() {
//            IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
//            IdlingRegistry.getInstance().register(dataBindingIdlingResource)
//        }
//
//        @After
//        fun unregisterIdlingResource() {
//            IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
//            IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
//        }
//
//        @Test
//        fun saveTask_assureDisplayed() = runBlocking {
//            tasksRepository.saveTask(Task("title", "description"))
//
//            val scenario = ActivityScenario.launch(MainActivity::class.java)
//            dataBindingIdlingResource.monitorActivity(scenario)
//
//        onView(withText("title")).check(matches(isDisplayed()))
//        onView(withText("description")).check(matches(isDisplayed()))
//        onView(withId(R.id.task_item_check_box)).check(matches(isChecked()))
//
//        onView(withId(R.id.add_task_fab)).perform(click())
//        onView(withId(R.id.title_text_input)).check(matches(isDisplayed()))
//        pressBack()
//        onView(withId(R.id.completed_tasks_chip)).perform(click())
//        onView(withId(R.id.empty_layout)).check(matches(isDisplayed()))
//
//        scenario.close()
//    }
//}