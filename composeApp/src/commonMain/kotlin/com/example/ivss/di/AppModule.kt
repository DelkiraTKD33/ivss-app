package com.example.ivss.di

import com.example.ivss.data.remote.IvssApiClient
import com.example.ivss.data.repository.ProfileRepositoryImpl
import com.example.ivss.data.repository.SettingsRepositoryImpl
import com.example.ivss.domain.repository.ProfileRepository
import com.example.ivss.domain.repository.SettingsRepository
import com.example.ivss.ui.forgotpassword.ui.ForgotPasswordViewModel
import com.example.ivss.ui.login.ui.LoginViewModel
import com.example.ivss.ui.profile.ui.ProfileViewModel
import com.example.ivss.ui.register.ui.RegisterViewModel
import com.example.ivss.ui.resetpassword.ui.ResetPasswordViewModel
import com.example.ivss.ui.settings.ui.SettingsViewModel
import com.example.ivss.ui.vacations.ui.VacationsViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::IvssApiClient)
    singleOf(::ProfileRepositoryImpl) { bind<ProfileRepository>() }
    singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::VacationsViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SettingsViewModel)
}
