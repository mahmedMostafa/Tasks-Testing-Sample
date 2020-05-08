package com.alien.brainean.todoapp.splash

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.alien.brainean.todoapp.FakeRepository
import com.alien.brainean.todoapp.MainCoroutineRule
import com.alien.brainean.todoapp.getOrAwaitValue
import com.alien.brainean.todoapp.ui.splash.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
class SplashViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: SplashViewModel

    private lateinit var fakeRepository: FakeRepository

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun init() {
        fakeRepository = FakeRepository()
        viewModel = SplashViewModel(fakeRepository)
    }

    //This is a flaky test make sure to test the logic again later
    @Test
    fun syncTask_loading() = mainCoroutineRule.dispatcher.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        viewModel.syncData()

        assertThat(viewModel.doneLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        //advanceTimeBy(2000)
        //assertThat(viewModel.doneLoading.getOrAwaitValue(), `is`(false))
    }

}