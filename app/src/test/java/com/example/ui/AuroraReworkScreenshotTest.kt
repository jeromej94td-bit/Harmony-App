package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AmbientBackground
import com.example.ui.components.HarmonyBottomNav
import com.example.ui.components.HarmonyTopBar
import com.example.ui.introspection.EyebrowCapsule
import com.example.ui.introspection.IntrospectionColors
import com.example.ui.introspection.IntrospectionPortal
import com.example.ui.introspection.MysticBackdrop
import com.example.ui.introspection.MysticButton
import com.example.ui.introspection.MysticCard
import com.example.ui.screens.GamesScreen
import com.example.ui.theme.HarmonyTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AuroraReworkScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gamesScreenAuroraPreview() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            HarmonyTheme(darkTheme = true) {
                AmbientBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            HarmonyTopBar(
                                userName = "Ralf",
                                partnerName = "J",
                                onProfileClick = {},
                                onRefresh = {}
                            )
                        },
                        bottomBar = {
                            HarmonyBottomNav(
                                selectedTab = 1,
                                onTabSelected = {},
                                appLanguage = "de"
                            )
                        }
                    ) { padding ->
                        GamesScreen(
                            answers = emptyList(),
                            packFilter = "all",
                            onSetFilter = {},
                            onCategoryClick = {},
                            onTopicClick = {},
                            onStartPack = {},
                            modifier = Modifier.padding(padding)
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "build/aurora-preview/games-screen.png"
        )
    }

    @Test
    fun introspectionAuroraPreview() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            MysticBackdrop {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EyebrowCapsule(text = "INTROSPECTION")
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Reise ins Unterbewusstsein",
                        color = IntrospectionColors.PrimaryText,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Höre nach innen. Das Portal begleitet dich.",
                        color = IntrospectionColors.SecondaryText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    IntrospectionPortal(size = 286.dp)
                    MysticCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = IntrospectionColors.SurfaceDark.copy(alpha = 0.82f),
                        borderColor = IntrospectionColors.PrimaryPink.copy(alpha = 0.46f)
                    ) {
                        Text(
                            text = "Welche Farbe erscheint vor deinem inneren Auge?",
                            modifier = Modifier.padding(18.dp),
                            color = IntrospectionColors.PrimaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    MysticButton(
                        text = "Antwort bestätigen",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            filePath = "build/aurora-preview/introspection-screen.png"
        )
    }
}
