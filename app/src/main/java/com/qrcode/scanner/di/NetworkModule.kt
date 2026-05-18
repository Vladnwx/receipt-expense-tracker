package com.qrcode.scanner.di

import com.qrcode.scanner.data.remote.FnsApiService
import com.qrcode.scanner.data.remote.GitHubReleaseApi
import com.qrcode.scanner.data.remote.ProverkachekaApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val userAgent = Interceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "ReceiptExpenseTracker/1.0")
                    .build()
            )
        }
        return OkHttpClient.Builder()
            .addInterceptor(userAgent)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://check.nalog.ru/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideFnsApiService(retrofit: Retrofit): FnsApiService {
        return retrofit.create(FnsApiService::class.java)
    }

    @Provides
    @Singleton
    @GitHubApi
    fun provideGitHubRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGitHubReleaseApi(@GitHubApi retrofit: Retrofit): GitHubReleaseApi {
        return retrofit.create(GitHubReleaseApi::class.java)
    }

    @Provides
    @Singleton
    @ProverkachekaApiQualifier
    fun provideProverkachekaRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://proverkacheka.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideProverkachekaApi(@ProverkachekaApiQualifier retrofit: Retrofit): ProverkachekaApi {
        return retrofit.create(ProverkachekaApi::class.java)
    }
}
