package com.waseem.locationtracking.app

import android.app.Application
import com.waseem.locationtracking.di.myModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App:Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(myModule)
        }
    }

}