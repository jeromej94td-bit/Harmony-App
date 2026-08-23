from pathlib import Path

models = Path('app/src/main/java/com/example/data/model/Models.kt').read_text(encoding='utf-8')
runner = Path('app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt').read_text(encoding='utf-8')

assert 'Wie möchtest du dein Ei am liebsten?' in models, 'missing fourth egg question'
for label in [
    '4 Minuten – Sehr flüssig', '5 Minuten – Flüssig', '6 Minuten – Weich & cremig',
    '7 Minuten – Weiches Eigelb', '8 Minuten – Cremiges Eigelb', '9 Minuten – Fast fest',
    '10 Minuten – Fest', '11 Minuten – Ziemlich fest', '12 Minuten – Sehr fest',
    '13 Minuten – Trocken', '14 Minuten – Sehr trocken', '15 Minuten – Übergart'
]:
    assert label in models, f'missing option: {label}'

assert 'isEggCookingQuestion' in runner, 'missing custom egg question route'
assert 'EggCookingQuestionGrid' in runner, 'missing custom egg grid'
assert 'EggCookingOptionCard' in runner, 'missing animated egg option cards'
assert 'row * 420L + column * 110L' in runner, 'missing row-wise domino timing'
assert 'rotationY' in runner and 'TransformOrigin(0f, 0.5f)' in runner, 'missing left-edge card reveal'
assert 'Brush.radialGradient' in runner and 'HarmonyPink' in runner and 'HarmonyPurple' in runner, 'missing Harmony dark/glow background'
assert 'egg_cooking_04' in runner and 'egg_cooking_15' in runner, 'missing egg image mapping'
print('egg cooking game invariants satisfied')
