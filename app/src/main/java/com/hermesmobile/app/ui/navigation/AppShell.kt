package com.hermesmobile.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.hermesmobile.app.data.ChatVm
import com.hermesmobile.app.glass.AppBackground
import com.hermesmobile.app.glass.GlassNavBarContent
import com.hermesmobile.app.glass.LocalGlassBackdrop
import com.hermesmobile.app.glass.glassNavBar
import com.hermesmobile.app.glass.rememberGlassBackdrop
import com.hermesmobile.app.ui.components.AnimatedBackdrop
import com.hermesmobile.app.ui.screens.ChatScreen
import com.hermesmobile.app.ui.screens.HistoryScreen
import com.hermesmobile.app.ui.screens.SettingsScreen
import com.hermesmobile.app.ui.theme.LiquidMotion
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class Tab(val title: String) {
    CHAT("Hermes"),
    HISTORY("Riwayat"),
    SETTINGS("Pengaturan"),
}

private val BarMargin = 16.dp
private val BarHeight = 64.dp

@Composable
fun AppShell(vm: ChatVm) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var showIntro by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(700)
        showIntro = false
    }

    BackHandler(enabled = tab != 0) { tab = 0 }

    val backdrop = rememberGlassBackdrop()
    var barPressed by remember { mutableStateOf(false) }
    val barScale by animateFloatAsState(
        targetValue = if (barPressed) 1.04f else 1f,
        animationSpec = LiquidMotion.press(),
        label = "barScale",
    )

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    SmallTopAppBar(
                        title = Tab.entries.getOrElse(tab) { Tab.CHAT }.title,
                        color = Color.Transparent,
                        actions = {
                            if (tab == Tab.CHAT.ordinal) {
                                IconButton(onClick = { vm.newChat(open = true) }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Obrolan baru")
                                }
                            }
                            IconButton(onClick = { tab = Tab.SETTINGS.ordinal }) {
                                Icon(Icons.Rounded.Settings, contentDescription = "Pengaturan")
                            }
                        },
                    )
                },
            ) { padding ->
                val ld = LocalLayoutDirection.current
                val contentPadding = PaddingValues(
                    start = padding.calculateStartPadding(ld),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(ld),
                    bottom = padding.calculateBottomPadding(),
                )

                Box(Modifier.fillMaxSize()) {
                    // Layer pengambilan: latar + seluruh konten halaman.
                    Box(Modifier.fillMaxSize().layerBackdrop(backdrop)) {
                        AppBackground()
                        AnimatedBackdrop(tabIndex = tab)
                        AnimatedContent(
                            targetState = tab,
                            transitionSpec = {
                                val forward = targetState > initialState
                                val enter: (Int) -> Int = { if (forward) it / 3 else -it / 3 }
                                val exit: (Int) -> Int = { if (forward) -it / 3 else it / 3 }
                                (fadeIn(LiquidMotion.snappy()) +
                                    slideInHorizontally(LiquidMotion.gentle(), enter) +
                                    scaleIn(initialScale = 0.96f, animationSpec = LiquidMotion.gentle())
                                    ).togetherWith(
                                    fadeOut(LiquidMotion.snappy()) +
                                        slideOutHorizontally(LiquidMotion.snappy(), exit) +
                                        scaleOut(targetScale = 0.96f, animationSpec = LiquidMotion.snappy()),
                                )
                            },
                            label = "page",
                        ) { index ->
                            Box(Modifier.fillMaxSize().padding(contentPadding)) {
                                when (Tab.entries.getOrElse(index) { Tab.CHAT }) {
                                    Tab.CHAT -> ChatScreen(vm = vm, onNewChat = { vm.newChat(open = true) })
                                    Tab.HISTORY -> HistoryScreen(vm = vm)
                                    Tab.SETTINGS -> SettingsScreen(vm = vm)
                                }
                            }
                        }
                    }

                    // Nav bar kaca mengambang — sibling di luar layer pengambilan.
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(start = BarMargin, end = BarMargin, bottom = BarMargin)
                                .fillMaxWidth()
                                .height(BarHeight)
                                .graphicsLayer { scaleX = barScale; scaleY = barScale },
                        ) {
                            Box(
                                Modifier.matchParentSize().glassNavBar(
                                    backdrop = backdrop,
                                    shape = RoundedCornerShape(50),
                                ),
                            )
                            GlassNavBarContent(
                                items = listOf(
                                    Icons.Rounded.ChatBubbleOutline to Tab.CHAT.title,
                                    Icons.Rounded.History to Tab.HISTORY.title,
                                    Icons.Rounded.Settings to Tab.SETTINGS.title,
                                ),
                                selectedIndex = tab,
                                onSelect = { tab = it },
                                modifier = Modifier.matchParentSize(),
                                backdrop = backdrop,
                                contentHeight = BarHeight,
                                onBarPressedChange = { barPressed = it },
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showIntro,
            enter = fadeIn(LiquidMotion.snappy()) + scaleIn(
                initialScale = 0.86f,
                animationSpec = LiquidMotion.gentle(),
            ),
            exit = fadeOut(LiquidMotion.snappy()) + scaleOut(
                targetScale = 1.08f,
                animationSpec = LiquidMotion.snappy(),
            ),
        ) {
            IntroOverlay()
        }
    }
}

@Composable
private fun IntroOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MiuixTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "H",
                    color = MiuixTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                "Hermes Mobile",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 14.dp),
            )
            Text(
                "Prompt dari saku, agen di mesinmu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
