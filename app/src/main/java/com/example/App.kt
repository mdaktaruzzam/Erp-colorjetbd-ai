package com.example

import android.app.Application
import com.example.data.FirebaseService

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseService.initialize(this)
    }
}
