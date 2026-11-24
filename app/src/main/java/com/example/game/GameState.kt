package com.example.game

import androidx.compose.runtime.Immutable

@Immutable
data class GameUiState(
  val playState: String = "ready",
  val score: Int = 0,
  val coins: Int = 0,
  val lives: Int = 3,
  val round: Int = 1,
  val bestScore: Int = 0,
  val round2Unlocked: Boolean = false,
  val isPaused: Boolean = false,
  val isMuted: Boolean = false,
  val hapticsEnabled: Boolean = true,
)
