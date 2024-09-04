package com.waseem.locationtracking.di


import android.content.Context
import android.location.Location
import com.waseem.locationtracking.utils.helper.LocationTracker
import com.waseem.locationtracking.utils.helper.PreferenceHelper
import org.koin.dsl.module

val myModule = module {
    // declaration of singleton instances
//    single { provideRetrofit() }
    single { PreferenceHelper(get()) }
    single { LocationTracker(get()) }

//    single { (context: Context, locationCallback: (Location?) -> Unit) ->
//        LocationTracker(context, locationCallback)
//    }

//    factory { provideApi(get()) }
//    factory { RemoteDataSource(get()) }
//
//    // repositories
//    single<LoginRepository> { LoginRepositoryImpl(get()) }
//    single<AllUserRepository> { AllUserRepositoryImpl(get()) }
//
//    // Use cases
//    factory { LoginUseCase(get()) }
//    factory { AllUserUseCase(get()) }
//
//    // View models
//    viewModel { LoginViewModel(get(), get()) }
//    viewModel { AllUserViewModel(get(), get()) }
}