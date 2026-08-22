#!/usr/bin/env python3
from pathlib import Path

path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")
text = path.read_text(encoding="utf-8")

old = '''        if (skipNextTotEntrance) {
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

new = '''        if (skipNextTotEntrance) {
            topOffsetY.snapTo(0f)
            bottomOffsetY.snapTo(0f)
            topTilt.snapTo(0f)
            bottomTilt.snapTo(0f)
            oderScale.snapTo(1f)
            topFlip.snapTo(0f)
            bottomFlip.snapTo(0f)
            skipNextTotEntrance = false

            // Continue the final shuffle momentum on the same rotationY axis only.
            // No extra Z tilt or positional wobble: just a small, damped rotational settle.
            coroutineScope {
                launch { topFlip.animateTo(2.0f, tween(120, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(-2.0f, tween(120, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topFlip.animateTo(-1.0f, tween(150, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(1.0f, tween(150, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topFlip.animateTo(0.35f, tween(130, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(-0.35f, tween(130, easing = FastOutSlowInEasing)) }
            }
            coroutineScope {
                launch { topFlip.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
                launch { bottomFlip.animateTo(0f, tween(180, easing = FastOutSlowInEasing)) }
            }
            return@LaunchedEffect
        }
'''

if text.count(old) != 1:
    raise SystemExit(f"Expected exactly one old settle block, found {text.count(old)}")

path.write_text(text.replace(old, new, 1), encoding="utf-8")
print("Applied Tot settle along rotationY axis only")
