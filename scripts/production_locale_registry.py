#!/usr/bin/env python3
"""Single source of truth for the 41 additional Harmony production locales."""

NEW_LOCALES = [
    {"enum":"HUNGARIAN","code":"hu","native":"Magyar","english":"Hungarian","flag":"🇭🇺","stem":"Hungarian","target":"hu","rtl":False},
    {"enum":"ROMANIAN","code":"ro","native":"Română","english":"Romanian","flag":"🇷🇴","stem":"Romanian","target":"ro","rtl":False},
    {"enum":"BULGARIAN","code":"bg","native":"Български","english":"Bulgarian","flag":"🇧🇬","stem":"Bulgarian","target":"bg","rtl":False},
    {"enum":"UKRAINIAN","code":"uk","native":"Українська","english":"Ukrainian","flag":"🇺🇦","stem":"Ukrainian","target":"uk","rtl":False},
    {"enum":"RUSSIAN","code":"ru","native":"Русский","english":"Russian","flag":"🇷🇺","stem":"Russian","target":"ru","rtl":False},
    {"enum":"GREEK","code":"el","native":"Ελληνικά","english":"Greek","flag":"🇬🇷","stem":"Greek","target":"el","rtl":False},
    {"enum":"TURKISH","code":"tr","native":"Türkçe","english":"Turkish","flag":"🇹🇷","stem":"Turkish","target":"tr","rtl":False},
    {"enum":"ARABIC","code":"ar","native":"العربية","english":"Arabic","flag":"🇸🇦","stem":"Arabic","target":"ar","rtl":True},
    {"enum":"HEBREW","code":"he","native":"עברית","english":"Hebrew","flag":"🇮🇱","stem":"Hebrew","target":"he","rtl":True},
    {"enum":"PERSIAN","code":"fa","native":"فارسی","english":"Persian","flag":"🇮🇷","stem":"Persian","target":"fa","rtl":True},
    {"enum":"HINDI","code":"hi","native":"हिन्दी","english":"Hindi","flag":"🇮🇳","stem":"Hindi","target":"hi","rtl":False},
    {"enum":"BENGALI","code":"bn","native":"বাংলা","english":"Bengali","flag":"🇧🇩","stem":"Bengali","target":"bn","rtl":False},
    {"enum":"URDU","code":"ur","native":"اردو","english":"Urdu","flag":"🇵🇰","stem":"Urdu","target":"ur","rtl":True},
    {"enum":"TAMIL","code":"ta","native":"தமிழ்","english":"Tamil","flag":"🇮🇳","stem":"Tamil","target":"ta","rtl":False},
    {"enum":"TELUGU","code":"te","native":"తెలుగు","english":"Telugu","flag":"🇮🇳","stem":"Telugu","target":"te","rtl":False},
    {"enum":"MARATHI","code":"mr","native":"मराठी","english":"Marathi","flag":"🇮🇳","stem":"Marathi","target":"mr","rtl":False},
    {"enum":"GUJARATI","code":"gu","native":"ગુજરાતી","english":"Gujarati","flag":"🇮🇳","stem":"Gujarati","target":"gu","rtl":False},
    {"enum":"KANNADA","code":"kn","native":"ಕನ್ನಡ","english":"Kannada","flag":"🇮🇳","stem":"Kannada","target":"kn","rtl":False},
    {"enum":"MALAYALAM","code":"ml","native":"മലയാളം","english":"Malayalam","flag":"🇮🇳","stem":"Malayalam","target":"ml","rtl":False},
    {"enum":"THAI","code":"th","native":"ไทย","english":"Thai","flag":"🇹🇭","stem":"Thai","target":"th","rtl":False},
    {"enum":"VIETNAMESE","code":"vi","native":"Tiếng Việt","english":"Vietnamese","flag":"🇻🇳","stem":"Vietnamese","target":"vi","rtl":False},
    {"enum":"INDONESIAN","code":"id","native":"Bahasa Indonesia","english":"Indonesian","flag":"🇮🇩","stem":"Indonesian","target":"id","rtl":False},
    {"enum":"MALAY","code":"ms","native":"Bahasa Melayu","english":"Malay","flag":"🇲🇾","stem":"Malay","target":"ms","rtl":False},
    {"enum":"FILIPINO","code":"fil","native":"Filipino","english":"Filipino","flag":"🇵🇭","stem":"Filipino","target":"tl","rtl":False},
    {"enum":"BURMESE","code":"my","native":"မြန်မာ","english":"Burmese","flag":"🇲🇲","stem":"Burmese","target":"my","rtl":False},
    {"enum":"KHMER","code":"km","native":"ខ្មែរ","english":"Khmer","flag":"🇰🇭","stem":"Khmer","target":"km","rtl":False},
    {"enum":"LAO","code":"lo","native":"ລາວ","english":"Lao","flag":"🇱🇦","stem":"Lao","target":"lo","rtl":False},
    {"enum":"SWAHILI","code":"sw","native":"Kiswahili","english":"Swahili","flag":"🇰🇪","stem":"Swahili","target":"sw","rtl":False},
    {"enum":"AFRIKAANS","code":"af","native":"Afrikaans","english":"Afrikaans","flag":"🇿🇦","stem":"Afrikaans","target":"af","rtl":False},
    {"enum":"AMHARIC","code":"am","native":"አማርኛ","english":"Amharic","flag":"🇪🇹","stem":"Amharic","target":"am","rtl":False},
    {"enum":"YORUBA","code":"yo","native":"Yorùbá","english":"Yoruba","flag":"🇳🇬","stem":"Yoruba","target":"yo","rtl":False},
    {"enum":"IGBO","code":"ig","native":"Igbo","english":"Igbo","flag":"🇳🇬","stem":"Igbo","target":"ig","rtl":False},
    {"enum":"HAUSA","code":"ha","native":"Hausa","english":"Hausa","flag":"🇳🇬","stem":"Hausa","target":"ha","rtl":False},
    {"enum":"ZULU","code":"zu","native":"isiZulu","english":"Zulu","flag":"🇿🇦","stem":"Zulu","target":"zu","rtl":False},
    {"enum":"XHOSA","code":"xh","native":"isiXhosa","english":"Xhosa","flag":"🇿🇦","stem":"Xhosa","target":"xh","rtl":False},
    {"enum":"SOMALI","code":"so","native":"Soomaali","english":"Somali","flag":"🇸🇴","stem":"Somali","target":"so","rtl":False},
    {"enum":"ESTONIAN","code":"et","native":"Eesti","english":"Estonian","flag":"🇪🇪","stem":"Estonian","target":"et","rtl":False},
    {"enum":"LATVIAN","code":"lv","native":"Latviešu","english":"Latvian","flag":"🇱🇻","stem":"Latvian","target":"lv","rtl":False},
    {"enum":"LITHUANIAN","code":"lt","native":"Lietuvių","english":"Lithuanian","flag":"🇱🇹","stem":"Lithuanian","target":"lt","rtl":False},
    {"enum":"SLOVENIAN","code":"sl","native":"Slovenščina","english":"Slovenian","flag":"🇸🇮","stem":"Slovenian","target":"sl","rtl":False},
    {"enum":"SERBIAN","code":"sr","native":"Српски","english":"Serbian","flag":"🇷🇸","stem":"Serbian","target":"sr","rtl":False},
]

CORE_OVERRIDES = {
    "hu": {"Frage":"Kérdés","ODER":"VAGY"}, "ro": {"Frage":"Întrebare","ODER":"SAU"},
    "bg": {"Frage":"Въпрос","ODER":"ИЛИ"}, "uk": {"Frage":"Питання","ODER":"АБО"},
    "ru": {"Frage":"Вопрос","ODER":"ИЛИ"}, "el": {"Frage":"Ερώτηση","ODER":"Ή"},
    "tr": {"Frage":"Soru","ODER":"VEYA"}, "ar": {"Frage":"سؤال","ODER":"أو"},
    "he": {"Frage":"שאלה","ODER":"או"}, "fa": {"Frage":"سؤال","ODER":"یا"},
    "hi": {"Frage":"प्रश्न","ODER":"या"}, "bn": {"Frage":"প্রশ্ন","ODER":"অথবা"},
    "ur": {"Frage":"سوال","ODER":"یا"}, "ta": {"Frage":"கேள்வி","ODER":"அல்லது"},
    "te": {"Frage":"ప్రశ్న","ODER":"లేదా"}, "mr": {"Frage":"प्रश्न","ODER":"किंवा"},
    "gu": {"Frage":"પ્રશ્ન","ODER":"અથવા"}, "kn": {"Frage":"ಪ್ರಶ್ನೆ","ODER":"ಅಥವಾ"},
    "ml": {"Frage":"ചോദ്യം","ODER":"അല്ലെങ്കിൽ"}, "th": {"Frage":"คำถาม","ODER":"หรือ"},
    "vi": {"Frage":"Câu hỏi","ODER":"HOẶC"}, "id": {"Frage":"Pertanyaan","ODER":"ATAU"},
    "ms": {"Frage":"Soalan","ODER":"ATAU"}, "fil": {"Frage":"Tanong","ODER":"O"},
    "my": {"Frage":"မေးခွန်း","ODER":"သို့မဟုတ်"}, "km": {"Frage":"សំណួរ","ODER":"ឬ"},
    "lo": {"Frage":"ຄຳຖາມ","ODER":"ຫຼື"}, "sw": {"Frage":"Swali","ODER":"AU"},
    "af": {"Frage":"Vraag","ODER":"OF"}, "am": {"Frage":"ጥያቄ","ODER":"ወይም"},
    "yo": {"Frage":"Ìbéèrè","ODER":"TÀBÍ"}, "ig": {"Frage":"Ajụjụ","ODER":"MA Ọ BỤ"},
    "ha": {"Frage":"Tambaya","ODER":"KO"}, "zu": {"Frage":"Umbuzo","ODER":"NOMA"},
    "xh": {"Frage":"Umbuzo","ODER":"OKANYE"}, "so": {"Frage":"Su'aal","ODER":"AMA"},
    "et": {"Frage":"Küsimus","ODER":"VÕI"}, "lv": {"Frage":"Jautājums","ODER":"VAI"},
    "lt": {"Frage":"Klausimas","ODER":"ARBA"}, "sl": {"Frage":"Vprašanje","ODER":"ALI"},
    "sr": {"Frage":"Питање","ODER":"ИЛИ"},
}

for locale in NEW_LOCALES:
    stem = locale["stem"]
    locale["filename"] = f"{stem}Content.kt"
    locale["exact"] = f"EXACT_{locale['enum']}_CONTENT"
    locale["dynamic"] = f"localize{stem}DynamicContent"

BY_CODE = {item["code"]: item for item in NEW_LOCALES}
RTL_CODES = {item["code"] for item in NEW_LOCALES if item["rtl"]}
