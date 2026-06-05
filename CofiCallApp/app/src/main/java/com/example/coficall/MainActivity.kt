package com.example.coficall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.coficall.data.DataRepository
import com.example.coficall.data.DefaultDataRepository
import com.example.coficall.theme.CofiCallTheme
import com.example.coficall.ui.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var repository: DataRepository
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = DefaultDataRepository(applicationContext)
        viewModel = MainViewModel(repository)
        enableEdgeToEdge()
        setContent {
            CofiCallTheme(darkTheme = viewModel.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MainNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
