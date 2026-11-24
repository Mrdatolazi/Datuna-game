package com.example.game

import android.webkit.JavascriptInterface

class DatunaWebBridge(
  private val onState: (playState: String, score: Int, coins: Int, lives: Int, round: Int) -> Unit,
  private val onHapticEvent: (kind: String) -> Unit,
) {
  @JavascriptInterface
  fun onGameState(playState: String, score: Int, coins: Int, lives: Int, round: Int) {
    onState(playState, score, coins, lives, round)
  }

  @JavascriptInterface
  fun onHaptic(kind: String) {
    onHapticEvent(kind)
  }
}
