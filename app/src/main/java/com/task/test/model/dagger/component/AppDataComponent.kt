package com.task.test.model.dagger.component

import com.task.test.MyApp
import com.task.test.model.dagger.module.NetworkModule
import com.task.test.viewmodels.ResultViewModel
import dagger.Component
import javax.inject.Singleton

@Component(modules = [NetworkModule::class])
@Singleton
interface AppDataComponent {
    fun injectResultViewModel(resultViewModel: ResultViewModel)
}

