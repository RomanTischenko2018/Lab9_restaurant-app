package com.example.restaurant.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.restaurant.RestaurantApplication
import com.example.restaurant.di.AppContainer

/** Достаёт [AppContainer] из контекста создания ViewModel (используется во всех фабриках). */
fun CreationExtras.appContainer(): AppContainer =
    (this[APPLICATION_KEY] as RestaurantApplication).container
