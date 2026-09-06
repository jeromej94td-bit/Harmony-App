from pathlib import Path
import base64
import hashlib

root = Path('.')
parts = []
expected = [
    '49243359a91b0c3706afc01a08f269a358b7f868a634f902d93915070a69537b',
    '6e50c88de3e1c1e0bdf3c607d6d0638e7072b68b5f5515efacdef037f80ac9da',
    '9ad05a8cbcefac80fec089392c1ea90825450bda4cf4ab6611290fae8349e71d',
    'd880e344b831ed016a7794a81dca868a7aa8b7f58604e548ebb2af64ef42e929',
]
for index, digest_expected in enumerate(expected):
    text = (root / f'scripts/egg_compact_chunks/part{index:02d}.txt').read_text(encoding='utf-8').strip()
    digest = hashlib.sha256(text.encode('utf-8')).hexdigest()
    if digest != digest_expected:
        raise RuntimeError(f'compact egg chunk {index} checksum mismatch: {digest}')
    parts.append(text)
raw = base64.b64decode(''.join(parts), validate=True)
if len(raw) != 25722:
    raise RuntimeError(f'unexpected compact egg asset size: {len(raw)}')
out = root / 'app/src/main/res/drawable-nodpi/egg_cooking_guide.webp'
out.parent.mkdir(parents=True, exist_ok=True)
out.write_bytes(raw)
print(f'wrote {out} ({len(raw)} bytes)')
