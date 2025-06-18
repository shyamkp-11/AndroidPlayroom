package com.shyampatel.githubplayroom.screen.home

import com.shyampatel.core.common.RepoOwner
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.core.data.github.GithubRepository
import com.shyampatel.githubplayroom.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifyCount
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyCount
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    private lateinit var repository: GithubRepository
    private val loggedInRepoOwner = RepoOwner(
        "owner",
        "login",
        "name",
        "company",
        "avatarUrl",
        "htmlUrl",
        RepoOwnerType.USER
    )

    private lateinit var tokenFlow: MutableSharedFlow<Result<String?>>
    private lateinit var repoOwnerFlow: MutableSharedFlow<Result<RepoOwner?>>
    private lateinit var notificationEnabledFlow: MutableSharedFlow<Result<Boolean>>

    private fun initFlows() {
        tokenFlow = MutableSharedFlow()
        repoOwnerFlow = MutableSharedFlow()
        notificationEnabledFlow = MutableSharedFlow()
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun setUp() {
        initFlows()
        repository = mockk()
        every { repository.getUserAccessToken() } returns tokenFlow
        every { repository.getAuthenticatedOwner() } returns repoOwnerFlow
        every { repository.getNotificationEnabled() } returns notificationEnabledFlow
    }

    @Test
    fun isUserAuthenticatedLoggedIn() = runTest {
        setUp()
        val viewModel = HomeViewModel(
            repository = repository,
        )
        Assert.assertEquals(
            HomeViewModel.HomeState.Loading,
            viewModel.isUserAuthenticated.first()
        )
        tokenFlow.emit(Result.success("token"))
        repoOwnerFlow.emit(Result.success(loggedInRepoOwner))
        notificationEnabledFlow.emit(Result.success(true))

        Assert.assertEquals(
            HomeViewModel.HomeState.LoggedIn(
                authenticatedOwner = loggedInRepoOwner,
                notificationsEnabled = true
            ), viewModel.isUserAuthenticated.first()
        )

        tokenFlow.emit(Result.success(null))
//        repoOwnerFlow.emit(Result.success(null))
        Assert.assertEquals(viewModel.isUserAuthenticated.first(), HomeViewModel.HomeState.LoggedOut)

        verifyCount {
            1 * { repository.getUserAccessToken() }
            1 * { repository.getAuthenticatedOwner() }
            1 * { repository.getNotificationEnabled() }
        }
        confirmVerified(repository)
    }

    @Test
    fun toggleNotifications() = runTest {

        setUp()
        coEvery { repository.setNotificationEnabled(enabled = true) } returns Result.success(Unit)

        val viewModel = HomeViewModel(
            repository = repository,
        )
        launch {
            tokenFlow.emit(Result.success("token"))
            repoOwnerFlow.emit(Result.success(loggedInRepoOwner))
            notificationEnabledFlow.emit(Result.success(false))
        }
        Assert.assertEquals(false, (viewModel.isUserAuthenticated.drop(1).first() as HomeViewModel.HomeState.LoggedIn).notificationsEnabled)
        viewModel.toggleNotifications()
        runCurrent()
        notificationEnabledFlow.emit(Result.success(value = true))
        Assert.assertEquals(true, (viewModel.isUserAuthenticated.first() as HomeViewModel.HomeState.LoggedIn).notificationsEnabled)
        coVerifyCount {
            1 * { repository.getUserAccessToken() }
            1 * { repository.getAuthenticatedOwner() }
            1 * { repository.getNotificationEnabled() }
            1 * { repository.setNotificationEnabled(enabled = true) }
        }
        confirmVerified(repository)
    }

    @Test
    fun signOut() = runTest {

        setUp()
        coEvery { repository.signOut() } returns Result.success(Unit)

        val viewModel = HomeViewModel(
            repository = repository,
        )
        // Todo fix sharedflow to be able to receive logout state directly
        Assert.assertEquals(
            HomeViewModel.HomeState.Loading,
            viewModel.isUserAuthenticated.first()
        )
        tokenFlow.emit(Result.success("token"))
        repoOwnerFlow.emit(Result.success(loggedInRepoOwner))
        notificationEnabledFlow.emit(Result.success(true))
        Assert.assertEquals(HomeViewModel.HomeState.LoggedIn(loggedInRepoOwner, true), viewModel.isUserAuthenticated.first())
        viewModel.signOut{}
        runCurrent()
        tokenFlow.emit(Result.success(null))
        repoOwnerFlow.emit(Result.success(null))
        notificationEnabledFlow.emit(Result.success(false))
        Assert.assertEquals(HomeViewModel.HomeState.LoggedOut, viewModel.isUserAuthenticated.first())
        coVerifyCount {
            1 * { repository.getUserAccessToken() }
            1 * { repository.getAuthenticatedOwner() }
            1 * { repository.getNotificationEnabled() }
            1 * { repository.signOut() }
        }


        confirmVerified(repository)
    }
}