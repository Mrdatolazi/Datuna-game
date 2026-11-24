package com.example.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel(application: Application) : AndroidViewModel(application) {
  private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  private val _uiState =
    MutableStateFlow(
      GameUiState(
        bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
        round2Unlocked = prefs.getBoolean(KEY_ROUND2_UNLOCKED, false),
        isMuted = prefs.getBoolean(KEY_MUTED, false),
        hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
      )
    )
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  fun onGameStateUpdate(
    playState: String,
    score: Int,
    coins: Int,
    lives: Int,
    round: Int,
  ) {
    _uiState.update { current ->
      val newBest = maxOf(current.bestScore, score)
      val unlockedRound2 = current.round2Unlocked || round >= 2 || (playState == "win" && round == 1)
      if (newBest != current.bestScore) {
        prefs.edit().putInt(KEY_BEST_SCORE, newBest).apply()
      }
      if (unlockedRound2 && !current.round2Unlocked) {
        prefs.edit().putBoolean(KEY_ROUND2_UNLOCKED, true).apply()
      }
      current.copy(
        playState = playState,
        score = score,
        coins = coins,
        lives = lives,
        round = round,
        bestScore = newBest,
        round2Unlocked = unlockedRound2,
      )
    }
  }

  fun setPaused(paused: Boolean) {
    _uiState.update { it.copy(isPaused = paused) }
  }

  fun toggleMute() {
    _uiState.update { current ->
      val next = !current.isMuted
      prefs.edit().putBoolean(KEY_MUTED, next).apply()
      current.copy(isMuted = next)
    }
  }

  fun toggleHaptics() {
    _uiState.update { current ->
      val next = !current.hapticsEnabled
      prefs.edit().putBoolean(KEY_HAPTICS, next).apply()
      current.copy(hapticsEnabled = next)
    }
  }

  companion object {
    private const val PREFS_NAME = "datuna_prefs"
    private const val KEY_BEST_SCORE = "best_score"
    private const val KEY_ROUND2_UNLOCKED = "round2_unlocked"
    private const val KEY_MUTED = "muted"
    private const val KEY_HAPTICS = "haptics"
  }
}
