package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.ChatterTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.viewmodel.ChatterViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ChatterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val isAuthCompleted by viewModel.isAuthCompleted.collectAsStateWithLifecycle()
                    if (!isAuthCompleted) {
                        AuthScreen(
                            viewModel = viewModel,
                            onComplete = { viewModel.setAuthCompleted(true) }
                        )
                    } else {
                        MainAppScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
