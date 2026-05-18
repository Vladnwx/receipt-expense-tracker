package com.qrcode.scanner.di

import com.qrcode.scanner.domain.fns.FnsAuthService
import com.qrcode.scanner.domain.fns.FnsAuthServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FnsModule {

    @Binds
    @Singleton
    abstract fun bindFnsAuthService(impl: FnsAuthServiceImpl): FnsAuthService
}
