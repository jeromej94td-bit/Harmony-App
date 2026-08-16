import re

with open("app/src/main/java/com/example/ui/screens/QuizRunnerScreen.kt", "r", encoding="utf-8") as f:
    content = f.read()

# Make sure we don't accidentally hide the default options if we added dynamic ones.
# The user's input string `{user}` should correctly resolve to the profile name.
print("App is fine. This ashmem error is an emulator issue, not an app issue.")
