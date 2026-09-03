package com.example.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameStatus
import com.example.model.ScreenState
import com.example.ui.components.EchoBottomHUD
import com.example.ui.components.EchoCanvas
import com.example.ui.components.EchoToastBanner
import com.example.ui.components.EchoTopHUD
import com.example.ui.components.RotateToPortraitPrompt
import com.example.ui.dialogs.DeadlockDialog
import com.example.ui.dialogs.LevelSelectDialog
import com.example.ui.dialogs.RewardedAdDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.dialogs.ShopDialog
import com.example.ui.dialogs.SkinsDialog
import com.example.ui.dialogs.VictoryDialog
import com.example.ui.intro.IntroLandscapeScreen
import com.example.ui.menu.EchoMainMenu
import com.example.viewmodel.EchoGameViewModel

@Composable
fun EchoGameScreen(
    viewModel: EchoGameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Handle system back button to return to Main Menu from game
    BackHandler(enabled = state.screenState != ScreenState.MAIN_MENU && state.screenState != ScreenState.INTRO) {
        viewModel.returnToMainMenu()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("echo_game_scaffold"),
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.screenState) {
                ScreenState.INTRO -> {
                    // Intro starts in horizontal (landscape) mode
                    IntroLandscapeScreen(
                        onIntroFinished = { viewModel.finishIntro() }
                    )
                }

                ScreenState.MAIN_MENU -> {
                    EchoMainMenu(
                        state = state,
                        onPlay = { viewModel.startPlayingLevel() },
                        onDailyChallenge = { viewModel.startDailyChallenge() },
                        onOpenLevelSelect = { viewModel.setLevelSelectVisible(true) },
                        onOpenShop = { viewModel.setShopVisible(true) },
                        onOpenSkins = { viewModel.setSkinsVisible(true) },
                        onOpenSettings = { viewModel.setSettingsVisible(true) }
                    )
                }

                ScreenState.PLAYING_LEVEL, ScreenState.DAILY_CHALLENGE -> {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top HUD
                        EchoTopHUD(
                            state = state,
                            onBackToMenu = { viewModel.returnToMainMenu() },
                            onOpenLevelSelect = { viewModel.setLevelSelectVisible(true) },
                            onOpenShop = { viewModel.setShopVisible(true) }
                        )

                        // Interactive Canvas with mathematical collision engine
                        EchoCanvas(
                            state = state,
                            onPointerDown = viewModel::onPointerDown,
                            onPointerMove = viewModel::onPointerMove,
                            onPointerUp = viewModel::onPointerUp,
                            modifier = Modifier.weight(1f)
                        )

                        // Bottom Action Bar: Reset, Clear Echoes, Power-ups, Hint
                        EchoBottomHUD(
                            state = state,
                            onReset = { viewModel.restartLevel(clearEchoes = false) },
                            onClearEchoes = { viewModel.clearEchoesAction() },
                            onUseBreaker = { viewModel.useEchoBreaker() },
                            onActivateShrinker = { viewModel.activateEchoShrinkerWithAd() },
                            onHint = { viewModel.useHint() }
                        )
                    }
                }
            }

            // Orientation warning: If device is still landscape after intro ends
            if (state.screenState != ScreenState.INTRO && isLandscape) {
                RotateToPortraitPrompt()
            }

            // Toast feedback banner (top-center)
            EchoToastBanner(
                message = state.toastMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp)
            )

            // 1. Victory Dialog
            if (state.gameStatus == GameStatus.VICTORY) {
                VictoryDialog(
                    levelId = state.level.levelId,
                    echoCount = state.echoCountForLevel,
                    parEchoes = state.level.parEchoes,
                    onNextLevel = { viewModel.nextLevel() },
                    onReplay = { viewModel.restartLevel(clearEchoes = true) }
                )
            }

            // 2. Deadlock / Game Over Dialog
            if (state.gameStatus == GameStatus.DEADLOCK) {
                DeadlockDialog(
                    echoCount = state.echoCountForLevel,
                    onClearEchoesWithAd = { viewModel.startRewardedAdSimulation("CLEAR_ECHOES") },
                    onRestartLevel = { viewModel.restartLevel(clearEchoes = true) }
                )
            }

            // 3. Rewarded Ad Simulation Dialog
            if (state.isRewardedAdShowing) {
                RewardedAdDialog(
                    countdownSeconds = state.adCountdownSeconds,
                    onDismiss = { viewModel.dismissRewardedAd() }
                )
            }

            // 4. Shop Dialog (IAP Simulation)
            if (state.isShopDialogVisible) {
                ShopDialog(
                    isAdFree = state.isAdFree,
                    currentTokens = state.tokens,
                    currentBreakers = state.echoBreakers,
                    onPurchaseAdFree = { viewModel.purchaseAdFree() },
                    onPurchaseTokens = { count -> viewModel.purchaseTokens(count) },
                    onPurchaseBreakers = { count -> viewModel.purchaseBreakers(count) },
                    onDismiss = { viewModel.setShopVisible(false) }
                )
            }

            // 5. Level Selector Dialog (100 levels from Room)
            if (state.isLevelSelectVisible) {
                LevelSelectDialog(
                    levels = state.allLevels,
                    currentLevelIndex = state.currentLevelIndex,
                    completedLevels = state.completedLevels,
                    onSelectLevel = { idx ->
                        viewModel.setLevelSelectVisible(false)
                        viewModel.startPlayingLevel(idx)
                    },
                    onDismiss = { viewModel.setLevelSelectVisible(false) }
                )
            }

            // 6. Settings Dialog
            if (state.isSettingsDialogVisible) {
                SettingsDialog(
                    soundEnabled = state.soundEnabled,
                    hapticsEnabled = state.hapticsEnabled,
                    onToggleSound = { viewModel.toggleSound(it) },
                    onToggleHaptics = { viewModel.toggleHaptics(it) },
                    onResetAllProgress = { viewModel.resetAllGameProgress() },
                    onDismiss = { viewModel.setSettingsVisible(false) }
                )
            }

            // 7. Skins / Themes Dialog
            if (state.isSkinsDialogVisible) {
                SkinsDialog(
                    currentStroke = state.strokeTheme,
                    currentEcho = state.echoTheme,
                    onSelectStroke = { viewModel.setStrokeTheme(it) },
                    onSelectEcho = { viewModel.setEchoTheme(it) },
                    onDismiss = { viewModel.setSkinsVisible(false) }
                )
            }
        }
    }
}
