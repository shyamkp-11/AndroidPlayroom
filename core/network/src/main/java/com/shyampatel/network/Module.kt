package com.shyampatel.network

import android.content.Context
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.shyampatel.core.network.BuildConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit

fun getNetworkModule(applicationContext: Context, baseUrl: String, ioDispatcher: CoroutineDispatcher) = module {
    single<Call.Factory> {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().also {
                if (BuildConfig.DEBUG) {
                    it.level = HttpLoggingInterceptor.Level.BODY
                }
            })
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            // From NowInAndroidApp "We use callFactory lambda here with dagger.Lazy<Call.Factory>
            // to prevent initializing OkHttp on the main thread".
            //    .callFactory { okhttpCallFactory.get().newCall(it) }
            .callFactory{
                get<Call.Factory>().newCall(it)
            }
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single {
        get<Retrofit>().create(RetrofitGithubRepoNetworkApi::class.java)
    }

    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

    single<GithubRepoRemoteDataSource> {
        GithubRepoRetrofitDataSource(
            networkApi = get(),
            ioDispatcher = ioDispatcher
        )
    }

    single {
        ImageLoader.Builder(applicationContext)
            .callFactory { get<Call.Factory>() }
            .components { add(SvgDecoder.Factory()) }
            .respectCacheHeaders(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}