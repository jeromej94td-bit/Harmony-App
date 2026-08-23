from pathlib import Path
import re

models_path = Path("app/src/main/java/com/example/data/model/Models.kt")
quiz_path = Path("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt")

models = models_path.read_text(encoding="utf-8")
quiz = quiz_path.read_text(encoding="utf-8")

question = "Was wäre für dich schlimmer: nie wieder Pizza oder nie wieder Burger?"

# Keep the full German source string as the stable localization key, but make the
# question genuinely binary in the data: only Pizza or Burger remain selectable.
models_pattern = re.compile(
    r'''Question\(\s*"Was wäre für dich schlimmer: nie wieder Pizza oder nie wieder Burger\?"\s*,\s*listOf\(\s*"Nie wieder Pizza"\s*,\s*"Nie wieder Burger"\s*,\s*"Beides wäre schlimm"\s*,\s*"Ich finde eine Alternative"\s*\)\s*\)''',
    re.MULTILINE,
)
models_replacement = '''Question(
                    "Was wäre für dich schlimmer: nie wieder Pizza oder nie wieder Burger?",
                    listOf(
                        "Nie wieder Pizza",
                        "Nie wieder Burger"
                    )
                )'''
models, count = models_pattern.subn(models_replacement, models, count=1)
assert count == 1, f"Expected exactly one Pizza/Burger question block, found {count}"

# The long source question is intentionally retained as the translation lookup key.
# On the question screen, render only the suspenseful stem in every language by
# trimming the localized text at its colon while preserving the locale's question mark.
anchor = '''private const val QUESTION_FLOW_TWO_PI = 6.2831855f

private fun questionFlowColor(phase: Float): Color {'''
helper = '''private const val QUESTION_FLOW_TWO_PI = 6.2831855f
private const val PIZZA_BURGER_TENSION_QUESTION =
    "Was wäre für dich schlimmer: nie wieder Pizza oder nie wieder Burger?"

private fun compactPizzaBurgerQuestion(rawQuestion: String, localizedQuestion: String): String {
    if (rawQuestion != PIZZA_BURGER_TENSION_QUESTION) return localizedQuestion

    val colonIndex = listOf(
        localizedQuestion.indexOf(':'),
        localizedQuestion.indexOf('：')
    ).filter { it >= 0 }.minOrNull() ?: return localizedQuestion

    val questionMark = localizedQuestion.lastOrNull { it == '?' || it == '؟' || it == '？' } ?: '?'
    val stem = localizedQuestion
        .substring(0, colonIndex)
        .trim()
        .trimEnd('?', '؟', '？')
    return "$stem$questionMark"
}

private fun questionFlowColor(phase: Float): Color {'''
assert anchor in quiz, "Question-flow anchor not found"
quiz = quiz.replace(anchor, helper, 1)

old_render = '''                                    AnimatedQuestionCard(
                                        question = contentText(q?.q ?: ""),
                                        glitchAmount = glitchAmount
                                    )
                                }
                            } else {
                                AnimatedQuestionCard(question = contentText(q?.q ?: ""))
                            }
                            Spacer(modifier = Modifier.height(26.dp))'''
new_render = '''                                    AnimatedQuestionCard(
                                        question = compactPizzaBurgerQuestion(
                                            rawQuestion = q?.q ?: "",
                                            localizedQuestion = contentText(q?.q ?: "")
                                        ),
                                        glitchAmount = glitchAmount
                                    )
                                }
                            } else {
                                AnimatedQuestionCard(
                                    question = compactPizzaBurgerQuestion(
                                        rawQuestion = q?.q ?: "",
                                        localizedQuestion = contentText(q?.q ?: "")
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(26.dp))'''
assert old_render in quiz, "Question-card render block not found"
quiz = quiz.replace(old_render, new_render, 1)

old_options = '''                            val rawOptions = q?.options ?: emptyList()
                            val processedOptions = rawOptions.map { 
                                it.replace("{user}", profile.userName).replace("{partner}", profile.partnerName)
                            }
                            val isNie = pack.cat == "nie"
                            val fallbackText = if (isNie) tr("Überspringen", "Skip") else tr("Schreibe deine eigene Antwort", "Write your own answer")
                            val options = processedOptions + fallbackText'''
new_options = '''                            val isPizzaBurgerTensionQuestion = q?.q == PIZZA_BURGER_TENSION_QUESTION
                            val rawOptions = q?.options ?: emptyList()
                            val processedOptions = rawOptions.map {
                                it.replace("{user}", profile.userName).replace("{partner}", profile.partnerName)
                            }
                            val isNie = pack.cat == "nie"
                            val fallbackText = if (isNie) tr("Überspringen", "Skip") else tr("Schreibe deine eigene Antwort", "Write your own answer")
                            val options = if (isPizzaBurgerTensionQuestion) {
                                processedOptions.take(2)
                            } else {
                                processedOptions + fallbackText
                            }'''
assert old_options in quiz, "Quiz options block not found"
quiz = quiz.replace(old_options, new_options, 1)

models_path.write_text(models, encoding="utf-8")
quiz_path.write_text(quiz, encoding="utf-8")
print("Applied binary Pizza/Burger tension question update")
