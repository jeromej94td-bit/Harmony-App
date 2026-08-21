from pathlib import Path
import re
import shutil
import zipfile

ROOT = Path(__file__).resolve().parents[1]
ZIP_PATH = ROOT / "app/src/main/assets/drive_tot_assets.zip"
DRAWABLE = ROOT / "app/src/main/res/drawable"
PROVIDER = ROOT / "app/src/main/java/com/example/ui/components/TotImageProvider.kt"
MARKER = ROOT / "tmp/drive_tot_assets_marker.txt"

MAPPING = {
    # Getränke
    "Cappuccino": "drink_cappuccino",
    "Matcha-Latte": "drink_matcha_latte",
    "Heiße Schokolade": "drink_heisse_schokolade",
    "Eistee": "drink_schwarzer_eistee",
    "Minzlimonade": "drink_minzlimonade",
    "Fruchtpunsch": "drink_fruchtpunsch",
    "Bier": "drink_bier",
    "Rote-Bete-Saft": "drink_rote_bete_saft",
    "Coca-Cola": "drink_coca_cola",
    "Fanta": "drink_fanta",
    "Orangensaft": "drink_orangensaft",
    "Apfelsaft": "drink_apfelsaft",
    "Kaffee": "drink_kaffee",
    "Tee": "drink_tee",

    # Tiere
    "Hund": "animal_hund",
    "Katze": "animal_katze",
    "Singvogel": "animal_singvogel",
    "Pinguin": "animal_pinguin",
    "Kaninchen": "animal_kaninchen",
    "Otter": "animal_otter",
    "Roter Panda": "animal_roter_panda",
    "Fuchs": "animal_fuchs",
    "Meerschweinchen": "animal_meerschweinchen",
    "Giraffe": "animal_giraffe",
    "Löwe": "animal_loewe",
    "Gorilla": "animal_gorilla",
    "Meeresschildkröte": "animal_meeresschildkroete",
    "Igel": "animal_igel",
    "Tiger": "animal_tiger",
    "Wolf": "animal_wolf",
    "Adler": "animal_adler",
    "Delfin": "animal_delfin",

    # Aktivitäten & Hobbys
    "Töpfern": "hobby_toepfern",
    "Klavier spielen": "hobby_klavier",
    "Malen": "hobby_malen",
    "Zeichnen": "hobby_zeichnen",
    "Badminton": "hobby_badminton",
    "Mountainbike": "hobby_mountainbike",
    "Bowling": "hobby_bowling",
    "Holzwerken": "hobby_holzwerken",
    "Gitarre spielen": "hobby_gitarre",
    "Tennis": "hobby_tennis",
    "Brettspiele": "hobby_brettspiele",
    "Darts": "hobby_darts",

    # Reiseziele – Drive-Versionen sind bereits ohne eingebrannte Ortslabels aufbereitet.
    "Miami, USA": "travel_miami",
    "Bangkok, Thailand": "travel_bangkok",
    "Chicago, USA": "travel_chicago",
    "Barcelona, Spanien": "travel_barcelona",
    "Lissabon, Portugal": "travel_lissabon",
    "Kopenhagen, Dänemark": "travel_kopenhagen",
    "Prag, Tschechien": "travel_prag",
    "Budapest, Ungarn": "travel_budapest",
    "Tokyo, Japan": "travel_tokyo",
}


def install_assets() -> None:
    if not ZIP_PATH.exists():
        raise SystemExit(f"Missing asset bundle: {ZIP_PATH}")

    DRAWABLE.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(ZIP_PATH) as archive:
        members = [m for m in archive.namelist() if m.lower().endswith(".webp")]
        if len(members) != 53:
            raise SystemExit(f"Expected 53 Drive assets, found {len(members)}")
        for member in members:
            target = DRAWABLE / Path(member).name
            with archive.open(member) as src, target.open("wb") as dst:
                shutil.copyfileobj(src, dst)


def patch_provider() -> None:
    text = PROVIDER.read_text(encoding="utf-8")
    block_lines = ["        // DRIVE_TOT_RELEASE_IMAGES_BEGIN"]
    for label, resource in MAPPING.items():
        escaped = label.replace("\\", "\\\\").replace('"', '\\"')
        block_lines.append(f'        "{escaped}" to R.drawable.{resource},')
    block_lines.append("        // DRIVE_TOT_RELEASE_IMAGES_END")
    replacement = "\n".join(block_lines)

    pattern = re.compile(
        r"        // DRIVE_TOT_RELEASE_IMAGES_BEGIN.*?        // DRIVE_TOT_RELEASE_IMAGES_END",
        re.S,
    )
    patched, count = pattern.subn(replacement, text, count=1)
    if count != 1:
        raise SystemExit("Could not locate DRIVE_TOT_RELEASE_IMAGES marker block")

    # Guardrail: this import must never touch the Gourmet-Eis integration.
    if "custom_gourmet_eissorten" in replacement or "Vanille" in replacement:
        raise SystemExit("Ice-cream content must remain untouched")

    PROVIDER.write_text(patched, encoding="utf-8")


def cleanup() -> None:
    ZIP_PATH.unlink(missing_ok=True)
    MARKER.unlink(missing_ok=True)


if __name__ == "__main__":
    install_assets()
    patch_provider()
    cleanup()
    print(f"Installed {len(MAPPING)} local Drive image mappings and 53 drawable files.")
