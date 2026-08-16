import base64
import io
import time
from PIL import Image, ImageDraw

options_data = [
    ("Vanille Bourbon", (255, 248, 220), (120, 100, 40)),
    ("Belgische Schokolade", (90, 50, 35), (255, 230, 210)),
    ("Sizilianische Pistazie", (180, 210, 140), (40, 80, 20)),
    ("Piemont Haselnuss", (190, 140, 100), (60, 30, 10)),
    ("Erdbeer-Basilikum", (230, 80, 100), (255, 245, 245)),
    ("Mango-Passionsfrucht", (255, 170, 40), (80, 30, 0)),
    ("Salted Caramel", (210, 130, 40), (255, 248, 220)),
    ("Tonkabohne", (120, 100, 110), (255, 240, 245)),
    ("Matcha-Grüntee", (130, 180, 110), (20, 60, 20)),
    ("Schwarzer Sesam", (70, 70, 80), (240, 240, 245)),
    ("Zitrone-Ingwer Sorbet", (255, 235, 100), (90, 80, 0)),
    ("Himbeer-Rhabarber Sorbet", (220, 60, 110), (255, 235, 240)),
    ("Stracciatella Deluxe", (245, 245, 240), (40, 30, 30)),
    ("Caffè Espresso", (100, 65, 45), (255, 235, 210))
]

pairs = [
    ("Vanille Bourbon", "Belgische Schokolade"),
    ("Sizilianische Pistazie", "Piemont Haselnuss"),
    ("Erdbeer-Basilikum", "Mango-Passionsfrucht"),
    ("Salted Caramel", "Tonkabohne"),
    ("Matcha-Grüntee", "Schwarzer Sesam"),
    ("Zitrone-Ingwer Sorbet", "Himbeer-Rhabarber Sorbet"),
    ("Stracciatella Deluxe", "Caffè Espresso")
]

images_base64 = {}

for name, bg_color, text_color in options_data:
    img = Image.new("RGB", (400, 400), color=bg_color)
    draw = ImageDraw.Draw(img)
    draw.ellipse([60, 60, 340, 340], outline=text_color, width=8)
    draw.text((200, 200), name, fill=text_color, anchor="mm")
    
    buf = io.BytesIO()
    img.save(buf, format="JPEG", quality=85)
    b64 = base64.b64encode(buf.getvalue()).decode("utf-8")
    images_base64[name] = b64

version = int(time.time() * 1000)

lines = []
lines.append("package com.example.data")
lines.append("")
lines.append("/**")
lines.append(" * AUTO-GENERIERT für Gourmet Eis-Sorten")
lines.append(" */")
lines.append("object GeneratedHarmonyContent {")
lines.append(f"    const val VERSION: Long = {version}L")
lines.append("")
lines.append("    val CATEGORIES: List<GenCategory> = listOf(")
lines.append("        GenCategory(\"tot\", \"Das oder das?\", \"⚖️\", 0xFFFFC46BL)")
lines.append("    )")
lines.append("")
lines.append("    val PACKS: List<GenPack> = listOf(")
lines.append("        GenPack(")
lines.append("            id = \"gourmet_eis_sorten\",")
lines.append("            title = \"Gourmet Eis-Sorten\",")
lines.append("            cat = \"tot\",")
lines.append("            topic = \"unterhaltung\",")
lines.append("            type = \"tot\",")
lines.append("            tags = listOf(\"dasoderdas\", \"unterhaltung\", \"gourmet\"),")
lines.append("            pairs = listOf(")
for p1, p2 in pairs:
    lines.append(f"                \"{p1}\" to \"{p2}\",")
lines.append("            ),")
lines.append("            questions = emptyList()")
lines.append("        )")
lines.append("    )")
lines.append("")
lines.append("    val LINK_PACKS: List<GenLinkPack> = emptyList()")
lines.append("")
lines.append("    val IMAGES: Map<String, String> by lazy {")
lines.append("        mapOf(")
img_items = list(images_base64.items())
for idx, (name, _) in enumerate(img_items):
    comma = "" if idx == len(img_items) - 1 else ","
    lines.append(f"            \"{name}\" to i{idx}(){comma}")
lines.append("        )")
lines.append("    }")
lines.append("")

CHUNK = 2000
for idx, (name, b64) in enumerate(img_items):
    lines.append(f"    // {name}")
    lines.append(f"    private fun i{idx}(): String = buildString {{")
    pos = 0
    while pos < len(b64):
        chunk = b64[pos:pos+CHUNK]
        lines.append(f"        append(\"{chunk}\")")
        pos += CHUNK
    lines.append("    }")
    lines.append("")

lines.append("}")
lines.append("")

with open("app/src/main/java/com/example/data/GeneratedHarmonyContent.kt", "w") as f:
    f.write("\n".join(lines))

print("GeneratedHarmonyContent.kt written successfully!")
