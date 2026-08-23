from pathlib import Path

source = Path('app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt').read_text(encoding='utf-8')

question_anchor = '''if (isIntimacyPack) {
                                CinematicSandMaterialize(
                                    animationKey = questionAnimationKey,
                                    delayMillis = 0,
                                    totalDurationMillis = 1_900,'''

answer_anchor = '''delayMillis = 760 + optIdx * 500,
                                        totalDurationMillis = 2_400,'''

assert question_anchor in source, 'Nähe & Intimität question materialization must be exactly 1.9s (1_900 ms)'
assert answer_anchor in source, 'Answer options must remain 2.4s long and start 0.5s apart from the existing 760 ms base delay'
print('Nähe & Intimität question is 1.9s; answers are staggered by 0.5s with unchanged 2.4s duration.')
