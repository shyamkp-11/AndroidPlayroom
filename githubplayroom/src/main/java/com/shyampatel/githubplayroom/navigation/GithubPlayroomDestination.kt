package com.shyampatel.githubplayroom.navigation

import com.shyampatel.githubplayroom.R

enum class GithubPlayroomDestination(
    val titleTextId: Int,
) {
    SEARCH(
        titleTextId = R.string.search,
    ),
    HOME(
        titleTextId = R.string.app_name,
    ),
    MY_REPOSITORIES(
        titleTextId = R.string.my_repositories,
    ),
    MY_STARRED(
        titleTextId = R.string.starred_repositories,
    ),
}
