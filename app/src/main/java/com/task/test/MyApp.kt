package com.task.test

import android.app.Application
import com.task.test.model.dagger.component.AppDataComponent
import com.task.test.model.dagger.component.DaggerAppDataComponent

class MyApp : Application() {
    companion object {
        lateinit var instance: MyApp
            private set
    }

    lateinit var appComponent: AppDataComponent
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        initComponent()
    }

    private fun initComponent() {
        appComponent = DaggerAppDataComponent.builder().build()
    }

}