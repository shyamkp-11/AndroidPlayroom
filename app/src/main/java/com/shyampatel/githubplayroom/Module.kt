package com.shyampatel.githubplayroom

import com.shyampatel.githubplayroom.screen.home.HomeViewModel
import com.shyampatel.githubplayroom.screen.login.GithubLoginViewModel
import com.shyampatel.githubplayroom.screen.myrepo.MyRepositoriesViewModel
import com.shyampatel.githubplayroom.screen.mystarredrepo.MyStarredViewModel
import com.shyampatel.githubplayroom.screen.search.SearchReposViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun getAppModule() = module {
    viewModel {
        SearchReposViewModel(repository = get())
    }
    viewModel {
        HomeViewModel(repository = get())
    }
    viewModel {
        GithubLoginViewModel(repository = get())
    }
    viewModel {
        MyRepositoriesViewModel(repository = get())
    }
    viewModel {
        MyStarredViewModel(repository = get())
    }
}