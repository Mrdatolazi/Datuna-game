package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.game.DatunaWebBridge
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.ui.theme.DatunaBg
import com.example.ui.theme.DatunaGold
import com.example.ui.theme.DatunaGoldDeep
import com.example.ui.theme.DatunaGoldLight
import com.example.ui.theme.DatunaInk
import com.example.ui.theme.DatunaMutedInk
import com.example.ui.theme.DatunaSurface
import com.example.ui.theme.DatunaSurfaceElevated

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DatunaGameScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  val webView = remember {
    WebView(context).apply {
      layoutParams =
        ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT,
        )
      setBackgroundColor(AndroidColor.parseColor("#10141C"))
      setLayerType(View.LAYER_TYPE_HARDWARE, null)
      overScrollMode = View.OVER_SCROLL_NEVER
      isVerticalScrollBarEnabled = false
      isHorizontalScrollBarEnabled = false
      isHapticFeedbackEnabled = true
      setOnLongClickListener { true }
      isLongClickable = false

      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        useWideViewPort = true
        loadWithOverviewMode = true
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
        allowFileAccess = true
        cacheMode = WebSettings.LOAD_DEFAULT
      }
      webChromeClient = WebChromeClient()
    }
  }

  DisposableEffect(webView) {
    val bridge =
      DatunaWebBridge(
        onState = { playState, score, coins, lives, round ->
          webView.post {
            viewModel.onGameStateUpdate(playState, score, coins, lives, round)
          }
        },
        onHapticEvent = { kind ->
          webView.post {
            if (!viewModel.uiState.value.hapticsEnabled) return@post
            val flag =
              when (kind) {
                "hurt" -> HapticFeedbackConstants.LONG_PRESS
                "win", "powerup" -> HapticFeedbackConstants.CONTEXT_CLICK
                else -> HapticFeedbackConstants.KEYBOARD_TAP
              }
            webView.performHapticFeedback(flag)
          }
        },
      )

    webView.addJavascriptInterface(bridge, "DatunaAndroid")
    webView.webViewClient =
      object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
          super.onPageFinished(view, url)
          val muted = viewModel.uiState.value.isMuted
          val paused = viewModel.uiState.value.isPaused
          view?.evaluateJavascript(
            "window.__datuna && (window.__datuna.setMuted($muted), ${if (paused) "window.__datuna.pause()" else "void 0"});",
            null,
          )
        }
      }
    webView.loadUrl("file:///android_asset/index.html")

    onDispose {
      webView.removeJavascriptInterface("DatunaAndroid")
      webView.stopLoading()
      webView.destroy()
    }
  }

  LaunchedEffect(uiState.isMuted) {
    webView.evaluateJavascript("window.__datuna && window.__datuna.setMuted(${uiState.isMuted});", null)
  }

  LaunchedEffect(uiState.isPaused) {
    val cmd = if (uiState.isPaused) "pause()" else "resume()"
    webView.evaluateJavascript("window.__datuna && window.__datuna.$cmd;", null)
  }

  LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
    if (uiState.playState == "play" && !uiState.isPaused) {
      viewModel.setPaused(true)
    }
    webView.evaluateJavascript("window.__datuna && window.__datuna.pause();", null)
    webView.onPause()
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    webView.onResume()
    if (!uiState.isPaused) {
      webView.evaluateJavascript("window.__datuna && window.__datuna.resume();", null)
    }
  }

  BackHandler(enabled = uiState.isPaused || uiState.playState == "play") {
    if (uiState.isPaused) {
      viewModel.setPaused(false)
    } else if (uiState.playState == "play") {
      viewModel.setPaused(true)
    }
  }

  Box(modifier = modifier.fillMaxSize().background(DatunaBg)) {
    AndroidView(
      factory = { webView },
      modifier = Modifier.fillMaxSize().testTag("game_webview"),
    )

    AnimatedVisibility(
      visible = uiState.playState == "play" && !uiState.isPaused,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier =
        Modifier
          .align(Alignment.TopStart)
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(start = 10.dp, top = 40.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = Color(0x9910141C),
        border = BorderStroke(1.dp, Color(0x4DFFFFFF)),
      ) {
        IconButton(
          onClick = { viewModel.setPaused(true) },
          modifier = Modifier.minimumInteractiveComponentSize().size(48.dp).testTag("pause_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Pause,
            contentDescription = stringResource(R.string.pause_game),
            tint = DatunaInk,
            modifier = Modifier.size(22.dp),
          )
        }
      }
    }

    AnimatedVisibility(
      visible = uiState.isPaused,
      enter = fadeIn() + scaleIn(initialScale = 0.94f, animationSpec = spring()),
      exit = fadeOut() + scaleOut(targetScale = 0.94f),
    ) {
      PauseOverlay(
        uiState = uiState,
        onResume = { viewModel.setPaused(false) },
        onRestart = { round ->
          viewModel.setPaused(false)
          webView.evaluateJavascript("window.__datuna && window.__datuna.restart($round);", null)
        },
        onToggleMute = viewModel::toggleMute,
        onToggleHaptics = viewModel::toggleHaptics,
      )
    }
  }
}

@Composable
private fun PauseOverlay(
  uiState: GameUiState,
  onResume: () -> Unit,
  onRestart: (Int) -> Unit,
  onToggleMute: () -> Unit,
  onToggleHaptics: () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.68f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {},
        )
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = DatunaSurface,
      border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(DatunaGoldLight, DatunaGoldDeep))),
      shadowElevation = 18.dp,
      modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
    ) {
      Column(
        modifier =
          Modifier
            .background(
              Brush.verticalGradient(
                colors = listOf(DatunaSurfaceElevated.copy(alpha = 0.72f), DatunaSurface),
              )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.ic_datuna_logo),
              contentDescription = stringResource(R.string.app_name),
              contentScale = ContentScale.Crop,
              modifier =
                Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .border(2.dp, DatunaGold, CircleShape),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = stringResource(R.string.paused_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = DatunaGoldLight,
              )
              Text(
                text = "${stringResource(R.string.round_label)} ${uiState.round}",
                style = MaterialTheme.typography.labelLarge,
                color = DatunaMutedInk,
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip(label = stringResource(R.string.score_label), value = uiState.score.toString())
            StatChip(label = stringResource(R.string.coins_label), value = uiState.coins.toString())
            StatChip(
              label = stringResource(R.string.best_score_label),
              value = uiState.bestScore.toString(),
              highlight = true,
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onResume,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = DatunaGold,
                contentColor = Color(0xFF1E1302),
              ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            modifier = Modifier.weight(1.2f).height(48.dp).testTag("resume_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.PlayArrow,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.resume_game),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
            )
          }

          OutlinedButton(
            onClick = { onRestart(1) },
            border = BorderStroke(1.dp, Color(0x55FFFFFF)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("restart_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Refresh,
              contentDescription = null,
              tint = DatunaInk,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.restart_game),
              color = DatunaInk,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
            )
          }

          if (uiState.round2Unlocked) {
            OutlinedButton(
              onClick = { onRestart(2) },
              border = BorderStroke(1.dp, DatunaGold.copy(alpha = 0.65f)),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
              modifier = Modifier.weight(1f).height(48.dp).testTag("round_two_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Flag,
                contentDescription = null,
                tint = DatunaGoldLight,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = stringResource(R.string.round_two),
                color = DatunaGoldLight,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          FilledTonalButton(
            onClick = onToggleMute,
            colors =
              ButtonDefaults.filledTonalButtonColors(
                containerColor = DatunaSurfaceElevated,
                contentColor = if (uiState.isMuted) DatunaMutedInk else DatunaGoldLight,
              ),
            modifier = Modifier.weight(1f).height(48.dp).testTag("mute_button"),
          ) {
            Icon(
              imageVector =
                if (uiState.isMuted) Icons.AutoMirrored.Rounded.VolumeOff
                else Icons.AutoMirrored.Rounded.VolumeUp,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text =
                stringResource(if (uiState.isMuted) R.string.sound_off else R.string.sound_on),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
            )
          }

          FilledTonalButton(
            onClick = onToggleHaptics,
            colors =
              ButtonDefaults.filledTonalButtonColors(
                containerColor = DatunaSurfaceElevated,
                contentColor = if (uiState.hapticsEnabled) DatunaGoldLight else DatunaMutedInk,
              ),
            modifier = Modifier.weight(1f).height(48.dp).testTag("haptics_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Vibration,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text =
                stringResource(
                  if (uiState.hapticsEnabled) R.string.haptics_on else R.string.haptics_off
                ),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = stringResource(R.string.controls_hint),
          style = MaterialTheme.typography.labelMedium,
          color = DatunaMutedInk,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
private fun StatChip(
  label: String,
  value: String,
  highlight: Boolean = false,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (highlight) DatunaGold.copy(alpha = 0.16f) else DatunaSurfaceElevated,
    border =
      BorderStroke(
        width = 1.dp,
        color = if (highlight) DatunaGold.copy(alpha = 0.5f) else Color(0x26FFFFFF),
      ),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        fontSize = 10.sp,
        color = if (highlight) DatunaGoldLight else DatunaMutedInk,
      )
      Text(
        text = value,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = if (highlight) DatunaGoldLight else DatunaInk,
      )
    }
  }
}
