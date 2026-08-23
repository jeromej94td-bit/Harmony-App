from pathlib import Path

path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")
text = path.read_text(encoding="utf-8")

# Android video player + Compose bridge.
imports_anchor = "package com.example.ui.screens\n\n"
imports = "package com.example.ui.screens\n\nimport android.net.Uri\nimport android.widget.VideoView\n"
assert imports_anchor in text, "package/import anchor not found"
text = text.replace(imports_anchor, imports, 1)

view_anchor = "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.window.Dialog\n"
view_replacement = "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.viewinterop.AndroidView\nimport androidx.compose.ui.window.Dialog\n"
assert view_anchor in text, "AndroidView import anchor not found"
text = text.replace(view_anchor, view_replacement, 1)

policy_anchor = "import com.example.ui.ActivePackRun\nimport com.example.ui.contentText\n"
policy_replacement = "import com.example.ui.ActivePackRun\nimport com.example.ui.UNIVERSAL_STUDIOS_INTRO_PACK_ID\nimport com.example.ui.contentText\nimport com.example.ui.shouldPlayUniversalStudiosIntro\n"
assert policy_anchor in text, "intro policy import anchor not found"
text = text.replace(policy_anchor, policy_replacement, 1)

runner_anchor = "@Composable\nfun QuizRunnerScreen("
player = '''@Composable
private fun UniversalStudiosIntroPlayer(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videoUri = remember(context) {
        Uri.parse("android.resource://${context.packageName}/${R.raw.universal_studios_intro}")
    }

    AndroidView(
        factory = { viewContext ->
            VideoView(viewContext).apply {
                setBackgroundColor(android.graphics.Color.rgb(9, 1, 15))
                setVideoURI(videoUri)
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    start()
                }
                setOnCompletionListener { onFinished() }
                setOnErrorListener { _, _, _ ->
                    onFinished()
                    true
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(HarmonyBg)
            .testTag("universal_studios_intro_video")
    )
}

'''
assert runner_anchor in text, "QuizRunnerScreen anchor not found"
text = text.replace(runner_anchor, player + runner_anchor, 1)

state_anchor = '''    val context = LocalContext.current
    val pack = activeRun.pack
    val totalLen = if (pack.type == "tot") pack.pairs.size else pack.questions.size
'''
state_replacement = '''    val context = LocalContext.current
    val pack = activeRun.pack
    val totalLen = if (pack.type == "tot") pack.pairs.size else pack.questions.size
    val shouldStartUniversalIntro = shouldPlayUniversalStudiosIntro(pack.id, activeRun.currentIndex)
    var showUniversalIntro by remember(pack.id) { mutableStateOf(shouldStartUniversalIntro) }
'''
assert state_anchor in text, "runner state anchor not found"
text = text.replace(state_anchor, state_replacement, 1)

column_anchor = '''            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
'''
column_replacement = '''            AnimatedVisibility(
                visible = !showUniversalIntro,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing)
                ) + scaleIn(
                    initialScale = 0.985f,
                    animationSpec = tween(durationMillis = 820, easing = FastOutSlowInEasing)
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
'''
assert column_anchor in text, "runner root Column anchor not found"
text = text.replace(column_anchor, column_replacement, 1)

close_anchor = '''                }
            }

            // Exit Confirm Dialog
'''
close_replacement = '''                }
            }
            }

            // Exit Confirm Dialog
'''
assert close_anchor in text, "runner Column close anchor not found"
text = text.replace(close_anchor, close_replacement, 1)

overlay_anchor = '''            }
        }
    }
}

private fun optionAccentColor(number: Int): Color = when (number) {
'''
overlay_replacement = '''            }

            if (showUniversalIntro) {
                UniversalStudiosIntroPlayer(
                    onFinished = { showUniversalIntro = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private fun optionAccentColor(number: Int): Color = when (number) {
'''
assert overlay_anchor in text, "intro overlay insertion anchor not found"
text = text.replace(overlay_anchor, overlay_replacement, 1)

# Sanity guards: the resource is bound only to the requested pack policy and the player is non-looping.
assert "UNIVERSAL_STUDIOS_INTRO_PACK_ID" in text
assert "showUniversalIntro = false" in text
assert "mediaPlayer.isLooping = false" in text

path.write_text(text, encoding="utf-8")
print("Applied Universal Studios one-shot intro integration")
# render-pipeline-trigger: pull_request_target workflow installed on main
