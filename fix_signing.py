with open("app/build.gradle.kts", "r") as f:
    text = f.read()

text = text.replace(
    'val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"',
    'val keystorePath = System.getenv("KEYSTORE_PATH") ?: (if (file("${rootDir}/my-upload-key.jks").exists()) "${rootDir}/my-upload-key.jks" else "${rootDir}/debug.keystore")'
)

with open("app/build.gradle.kts", "w") as f:
    f.write(text)
