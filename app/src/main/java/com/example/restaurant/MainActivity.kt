package com.example.restaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.restaurant.ui.navigation.RestaurantNavHost
import com.example.restaurant.ui.theme.RestaurantAppTheme

/** Единственная Activity: хостит Compose-навигацию. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestaurantAppTheme {
                RestaurantNavHost()
            }
        }
    }
}
