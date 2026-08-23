from pathlib import Path

source = Path('app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt').read_text(encoding='utf-8')

question_anchor = '''if (isIntimacyPack) {
                                CinematicSandMaterialize(
                                    animationKey = questionAnimationKey,
                                    delayMillis = 0,
                                    totalDurationMillis = 1_900,'''

assert question_anchor in source, 'Nähe & Intimität question materialization must be exactly 1.9s (1_900 ms)'
assert 'totalDurationMillis = 2_400' in source, 'Answer materialization duration should remain unchanged at 2.4s'
print('Nähe & Intimität question duration is 1.9s; answer timing unchanged.')
