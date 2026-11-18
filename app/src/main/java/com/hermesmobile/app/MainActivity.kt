package com.hermesmobile.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import com.hermesmobile.app.data.ChatVm
import com.hermesmobile.app.ui.navigation.AppShell
import com.hermesmobile.app.ui.theme.Accent
import com.hermesmobile.app.ui.theme.LiquidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val vm = ChatVm(this)
        setContent {
            val mode = vm.settings.darkMode
            val dark = if (mode == 0) isSystemInDarkTheme() else mode == 2
            val accent = vm.settings.accent
            AnimatedContent(
                targetState = dark to accent,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "theme",
            ) { (d, a) ->
                LiquidTheme(accentColor = Accent.fromId(a), darkTheme = d) {
                    AppShell(vm = vm)
                }
            }
        }
    }
}
