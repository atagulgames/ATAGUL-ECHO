package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenState
import com.example.ui.EchoGameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.EchoGameViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    // User requirement: Intro starts in horizontal (landscape) mode
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

    setContent {
      MyApplicationTheme(darkTheme = false) {
        val gameViewModel: EchoGameViewModel = viewModel()
        val state by gameViewModel.uiState.collectAsState()

        // Transition to portrait (dikey) after intro finishes
        LaunchedEffect(state.screenState) {
          if (state.screenState != ScreenState.INTRO) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
          }
        }

        EchoGameScreen(viewModel = gameViewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    com.example.audio.HarmonicAudioEngine.resumeBgm()
  }

  override fun onPause() {
    super.onPause()
    com.example.audio.HarmonicAudioEngine.pauseBgm()
  }
}
