package com.shyampatel.githubplayroom

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.shyampatel.core.common.GithubRepoModel
import com.shyampatel.core.common.RepoOwnerType
import com.shyampatel.ui.theme.AndroidPlayroomTheme
import com.shyampatel.ui.theme.StarColor

@Composable
fun GithubRepoListItem(
    repo: GithubRepoModel,
    uriHandler: UriHandler,
    modifier: Modifier = Modifier,
    onStarClick: (() -> Unit)? = null,
    isRepoStarred: Boolean = false,
    showStarButton: Boolean = true,
    showPublicPrivate: Boolean = false,
) {
    val transition = updateTransition(isRepoStarred, label = "Star transition")
    val starColor by transition.animateColor(
        label = "star color",
        transitionSpec = {
            tween(durationMillis = 500)
        }
    ) { isStarred ->
        if (isStarred) StarColor else MaterialTheme.colorScheme.primary
    }
    val starScale by transition.animateFloat(
        label = "star scale",
        transitionSpec = {
            keyframes {
                durationMillis = 600
                1f at 0 using EaseInElastic
                1.8f at 200 using EaseOutBounce
                1f at 600
            }
        }
    ) { isStarred ->
        if (isStarred) 1f else 1f
    }
//    val animateScale = remember { Animatable(1f) }

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                RepoOwnerImage(
                    cropCircle = repo.ownerType == RepoOwnerType.USER,
                    repoOwnerImageUrl = repo.ownerAvatarUrl
                )
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .align(Alignment.CenterVertically),
                    text = repo.ownerLogin,
                    style = MaterialTheme.typography.labelMedium,
                )
                if (showStarButton) {
                    IconButton(
                        onClick = {
                            onStarClick?.invoke()
                            /*
                    scope.launch {
                        animateScale.stop()

                        animateScale.animateTo(1.5f, spring())
                        animateScale.animateTo(1f, tween(1000, 0, AnticipateOvershootInterpolator().toEasing()))
                    }
*/
                        },
                        enabled = onStarClick != null,
                        modifier = Modifier
                            .weight(0.15f)
//                    .scale(animateScale.value)
                            .scale(starScale)
                    ) {
                        Icon(
                            Icons.Filled.Stars,
                            contentDescription = stringResource(R.string.star_action),
                            tint = starColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            Row(modifier = Modifier
                .padding(top = 12.dp)) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                IconButton(
                    onClick = { uriHandler.openUri(repo.htmlUrl) },
                    modifier = Modifier
                        .padding(start = 3.dp, top = 4.dp)
                        .size(14.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Default.OpenInNew, contentDescription = "Open repository in browser")
                }
            }
            if(repo.description != null) {
                Text(
                    text = repo.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_star_outline_24),
                    contentDescription = "Stars",
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(14.dp)
                        .offset(x = (-1).dp)

                )
                Text(
                    text = "%,d".format(repo.stars),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 3.dp),
                )
                if (repo.language != null) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Programming language",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 10.dp)
                            .size(14.dp)

                    )
                    Text(
                        text = repo.language ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 3.dp),
                    )
                }
                if (showPublicPrivate) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Programming language",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 10.dp)
                            .size(8.dp)

                    )
                    Text(
                        text = if (repo.private) stringResource(R.string.private_repo) else stringResource(R.string.public_repo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun RepoOwnerImage(
    cropCircle: Boolean = false,
    repoOwnerImageUrl: String?,
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val imageLoader = rememberAsyncImagePainter(
        model = repoOwnerImageUrl,
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading
            isError = state is AsyncImagePainter.State.Error
        },
    )
    val isLocalInspection = LocalInspectionMode.current
    Box(
        modifier = Modifier
            .size(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            // Display a progress bar while loading
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }

        Image(
            modifier = if (cropCircle) {
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            } else {
                Modifier
                    .fillMaxSize()
                    .height(180.dp)
            },
            contentScale = ContentScale.Crop,
            painter = if (isError.not() && !isLocalInspection) {
                imageLoader
            } else {
                painterResource(com.shyampatel.ui.R.drawable.user)
            },
            contentDescription = null,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GithubRepoListItemPrev() {
    AndroidPlayroomTheme {
        var isStarred: Boolean by remember { mutableStateOf(true) }
        GithubRepoListItem(
            repo = GithubRepoModel(
                1,
                name = "repo name",
                fullName = "First Repo",
                stars = 1000,
                ownerId = 1,
                private = false,
                htmlUrl = "",
                ownerLogin = "owner name",
                ownerAvatarUrl = "",
                ownerType = RepoOwnerType.USER,
                description = "description",
                language = "language",
            ),
            onStarClick = { isStarred = !isStarred },
            isRepoStarred = isStarred,
            uriHandler = LocalUriHandler.current,
            showPublicPrivate = true
        )
    }
}

@Preview
@Composable
private fun RepoOwnerImagePrev() {
    RepoOwnerImage(repoOwnerImageUrl = null)
}