package com.vladnwx.receiptexpensetracker.di

import com.google.gson.Gson
import com.vladnwx.receiptexpensetracker.data.remote.cbr.CbrApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideCbrApi(client: OkHttpClient): CbrApi {
        return Retrofit.Builder()
            .baseUrl("https://www.cbr-xml-daily.ru/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CbrApi::class.java)
    }
}
