package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Datuna", appName)
  }

  @Test
  fun `viewmodel tracks best score and unlocks round 2`() {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.game.GameViewModel(app)
    vm.onGameStateUpdate("play", 500, 3, 3, 1)
    assertEquals(500, vm.uiState.value.bestScore)
    assertEquals(false, vm.uiState.value.round2Unlocked)

    vm.onGameStateUpdate("win", 1200, 8, 3, 1)
    assertEquals(1200, vm.uiState.value.bestScore)
    assertEquals(true, vm.uiState.value.round2Unlocked)
  }
}
