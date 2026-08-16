import build_full_language, generate_translations

all_texts = generate_translations.all_texts
trans = build_full_language.translations

unmapped = [t for t in all_texts if t not in trans]
print(f"Unmapped strings remaining: {len(unmapped)}")
print("Sample unmapped:")
for u in sorted(unmapped)[:20]:
    print(" *", repr(u))
