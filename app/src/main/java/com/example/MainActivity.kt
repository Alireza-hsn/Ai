package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.SinaMindScreen
import com.example.ui.theme.SinaMindTheme
import com.example.ui.viewmodel.SinaMindViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: SinaMindViewModel = viewModel()
      val isSleepMode by viewModel.isSleepMode.collectAsState()

      SinaMindTheme(isSleepMode = isSleepMode) {
        SinaMindScreen(viewModel = viewModel)
      }
    }
  }
}
