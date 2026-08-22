#!/usr/bin/env python3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCREEN = ROOT / "app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt"
text = SCREEN.read_text(encoding="utf-8")

old_branch = '''        if (skipNextTotEntrance) {
            topOffsetY.snapTo(0f)
            bottomOffsetY.snapTo(0f)
            topTilt.snapTo(0f)
            bottomTilt.snapTo(0f)
            oderScale.snapTo(1f)
            topFlip.snapTo(0f)
            bottomFlip.snapTo(0f)
            skipNextTotEntrance = false
            return@LaunchedEffect
        }
'''

new_branch = '''        if (skipNextTotEntrance) {
            topOffsetY.snapTo(0f)
            bottomOffsetY.snapTo(0f)
            topTilt.snapTo(0f)
            bottomTilt.snapTo(0f)
            oderScale.snapTo(1f)
            topFlip.snapTo(0f)
            bottomFlip.snapTo(0f)
            skipNextTotEntrance = false

            // A short damped 3D settle keeps the incoming pair feeling physical without
            // moving it away from its final position. Any new tap cancels these Animatables.
            coroutineScope {
                launch { topTilt.animateTo(0.9f, tween(120, easing = FastOutSlowInEasing)) }
                launch { bottomTilt.animateTo(-0.9f, tween(120, easing = FastOutSlowInEasing)) }
                launch { topFlip.animateTo(2.4f, tween(120, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(-2.4f, tween(120, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topTilt.animateTo(-0.55f, tween(150, easing = FastOutSlowInEasing)) }
                launch { bottomTilt.animateTo(0.55f, tween(150, easing = FastOutSlowInEasing)) }
                launch { topFlip.animateTo(-1.35f, tween(150, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(1.35f, tween(150, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topTilt.animateTo(0.22f, tween(130, easing = FastOutSlowInEasing)) }
                launch { bottomTilt.animateTo(-0.22f, tween(130, easing = FastOutSlowInEasing)) }
                launch { topFlip.animateTo(0.55f, tween(130, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(-0.55f, tween(130, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topTilt.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
                launch { bottomTilt.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
                launch { topFlip.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
            }
            return@LaunchedEffect
        }
'''

old_tail = '''            skipNextTotEntrance = true
            onPick(option)
            isAnimating = false
'''

new_tail = '''            skipNextTotEntrance = true
            onPick(option)
            isAnimating = false
            // tot_settle_wobble: the incoming pair is already clickable while its tiny
            // inertial settle runs in the keyed LaunchedEffect above.
'''

if old_branch not in text:
    raise SystemExit("Expected skipNextTotEntrance branch was not found")
if old_tail not in text:
    raise SystemExit("Expected post-shuffle tail was not found")

text = text.replace(old_branch, new_branch, 1)
text = text.replace(old_tail, new_tail, 1)
SCREEN.write_text(text, encoding="utf-8")
print("Applied subtle post-shuffle 3D settle wobble")
