package com.example.restaurant

import android.app.Application
import com.example.restaurant.di.AppContainer

/** Класс приложения: хранит единственный [AppContainer]. */
class RestaurantApplication : Application() {

    /** Контейнер зависимостей, доступный из фабрик ViewModel. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
