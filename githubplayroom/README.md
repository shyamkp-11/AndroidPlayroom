
Github Playroom sample
==================
Github Playroom is a sample app showcasing working with new android libraries such as Jetpack Compose (views, theming, animation, navigation), Koin, Kotlin coroutines, Jetpack Room, ViewModels and Retrofit build with clean architecture design.

## Screenshots
![Screenshot showing Home screen, Search repos screen and Starred repos screen](docs/Screenshots.png "Screenshot showing Home screen, Search repos screen and Starred repos screen")

# Features
This sample contains four screens: home screen, search repositories screen, starred repositories screen and my repositories screen. User can star a repository or unstar a github repository from the app if authenticated.

# Run the App
App can run in an <mark>authenticated mode</mark> or <mark>unauthenticated</mark>. Without authentication user will only be able to search github repositories and unable to access starred repositories or my repositories. To use in an authenticated mode do following:


1. Sign in into [Github Login](https://github.com/login) or create a new account.
2. [Go to `Create a new Github App`](https://github.com/settings/apps/new).
3. Give a unique name to the app. Set `Homepage URL` to https://www.github.com. Set `Callback URL` to https://www.github.com.

- [ ] Uncheck `Expire user authorization tokens`
- [x] Check `Request user authorization (OAuth) during installation`
- [ ] Uncheck `Active` under `Webhook`

    In `Repository permissions` under `Permissions`:


    - Give `Read-only` access to `Administration`


    In the ` Account permissions` :


    - Give `Read and write` access to `Starring`.


    Select `🔘 Any account` in `Where can this Github App be installed?`


    Click `Create GitHub App`.
4. Once created copy / save `Client ID`.
5. Click `Genereate a new client secret` and copy / save the client secret.
6. Clone the [Android App](https://github.com/shyamkp-11/GithubPlayroom) into Android Studio.
7. Go to **secrets.properties** file (If not there create one in the root folder of the project). And enter your    

    >CLIENT_ID=(Your client id)  
    CLIENT_SECRET=(Your client secret)  
    APP_NAME=(Your app name)
8. Gradle sync, clean and ▶️ Run the project.

## API mode
App is set to run using Github GraphQL as network layer using Apollo GrapgQL client dependency. To use Github Rest API using Retrofit, go to **gradle.properties** file and set `GITHUBPLAYROOM_API_MODE=REST` instead of `GRAPHQL`.  