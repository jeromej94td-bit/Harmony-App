import math
import struct
import wave
import subprocess
import os

SAMPLE_RATE = 44100

def generate_sine_wave(freq, duration, sample_rate=SAMPLE_RATE, volume=0.5):
    n_samples = int(duration * sample_rate)
    samples = []
    for i in range(n_samples):
        t = i / sample_rate
        val = math.sin(2 * math.pi * freq * t) * volume
        samples.append(val)
    return samples

def write_wav(filename, left_samples, right_samples, sample_rate=SAMPLE_RATE):
    n_samples = min(len(left_samples), len(right_samples))
    with wave.open(filename, 'w') as wf:
        wf.setnchannels(2)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        frames = bytearray()
        for i in range(n_samples):
            l = max(-32767, min(32767, int(left_samples[i] * 32767)))
            r = max(-32767, min(32767, int(right_samples[i] * 32767)))
            frames.extend(struct.pack('<hh', l, r))
        wf.writeframes(frames)

def generate_merlin_theme(filename, duration=32.0):
    n_samples = int(duration * SAMPLE_RATE)
    left = [0.0] * n_samples
    right = [0.0] * n_samples
    
    # Chord progression: Cm -> Abmaj7 -> Eb -> Gsus4 -> Cm (8s each)
    chords = [
        # (root freq, chord freqs, duration)
        ([130.81, 155.56, 196.00, 261.63, 392.00, 523.25]), # C minor / C4, G4, C5
        ([103.83, 155.56, 207.65, 261.63, 311.13, 415.30]), # Ab maj7
        ([155.56, 196.00, 233.08, 311.13, 392.00, 466.16]), # Eb
        ([98.00, 146.83, 196.00, 261.63, 293.66, 392.00])   # Gsus4 / G
    ]
    
    chord_len = duration / len(chords)
    
    for ci, chord in enumerate(chords):
        c_start = int(ci * chord_len * SAMPLE_RATE)
        c_end = int((ci + 1) * chord_len * SAMPLE_RATE)
        c_samples = c_end - c_start
        
        for i in range(c_samples):
            idx = c_start + i
            if idx >= n_samples: break
            t = i / SAMPLE_RATE
            
            # Envelope with smooth attack and release
            env = 1.0
            attack = 2.0
            release = 2.0
            if t < attack:
                env = 0.5 * (1 - math.cos(math.pi * t / attack))
            elif t > chord_len - release:
                env = 0.5 * (1 + math.cos(math.pi * (t - (chord_len - release)) / release))
                
            # LFO for soothing mystic vibrato / filter warmth
            lfo1 = 1.0 + 0.15 * math.sin(2 * math.pi * 0.2 * t)
            lfo2 = 1.0 + 0.15 * math.cos(2 * math.pi * 0.25 * t)
            
            val_l = 0.0
            val_r = 0.0
            for fi, freq in enumerate(chord):
                amp = 0.12 / (1.0 + fi * 0.3)
                detune_l = freq * (1.0 - 0.0015 * (fi + 1))
                detune_r = freq * (1.0 + 0.0015 * (fi + 1))
                val_l += math.sin(2 * math.pi * detune_l * t) * amp
                val_r += math.sin(2 * math.pi * detune_r * t) * amp
                # Warm sub harmonic
                val_l += 0.04 * math.sin(2 * math.pi * (freq * 0.5) * t)
                val_r += 0.04 * math.sin(2 * math.pi * (freq * 0.5) * t)
                
            # Shimmer overtone
            shimmer = 0.02 * math.sin(2 * math.pi * (chord[0] * 4.0) * t) * math.sin(2 * math.pi * 3.0 * t)
            
            left[idx] += (val_l * lfo1 + shimmer) * env * 0.7
            right[idx] += (val_r * lfo2 + shimmer) * env * 0.7
            
    # Mystic bell chimes at key beats
    chime_times = [0.5, 4.0, 8.5, 12.0, 16.5, 20.0, 24.5, 28.0]
    chime_notes = [523.25, 659.25, 783.99, 1046.50, 880.00, 659.25, 783.99, 1046.50]
    for ct, cf in zip(chime_times, chime_notes):
        c_idx = int(ct * SAMPLE_RATE)
        for i in range(int(3.5 * SAMPLE_RATE)):
            idx = c_idx + i
            if idx >= n_samples: break
            t = i / SAMPLE_RATE
            chime_env = math.exp(-2.2 * t)
            c_val = (math.sin(2 * math.pi * cf * t) + 0.3 * math.sin(2 * math.pi * cf * 2.0 * t) + 0.1 * math.sin(2 * math.pi * cf * 3.0 * t)) * chime_env * 0.15
            left[idx] += c_val * 0.8
            right[idx] += c_val * 0.6
            
    write_wav(filename, left, right)

def build_narration(text, output_mp3, bg_type="mystic"):
    # 1. Generate voice via flite
    flite_wav = f"/tmp/flite_raw.wav"
    subprocess.run([
        'ffmpeg', '-f', 'lavfi', '-i', f"flite=text='{text}':voice=slt",
        '-ar', '44100', '-ac', '2', '-y', flite_wav
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    
    # Get flite duration
    probe = subprocess.run([
        'ffprobe', '-v', 'error', '-show_entries', 'format=duration',
        '-of', 'default=noprint_wrappers=1:nokey=1', flite_wav
    ], capture_output=True, text=True, check=True)
    voice_dur = float(probe.stdout.strip())
    total_dur = voice_dur + 3.0
    
    # 2. Generate atmospheric pad / chimes
    pad_wav = f"/tmp/pad_temp.wav"
    n_samples = int(total_dur * SAMPLE_RATE)
    l = [0.0] * n_samples
    r = [0.0] * n_samples
    
    # Atmospheric intro chime & pad
    freqs = [261.63, 329.63, 392.00, 523.25] if bg_type == "mystic" else [220.00, 277.18, 329.63, 440.00]
    for i in range(n_samples):
        t = i / SAMPLE_RATE
        fade = 1.0
        if t < 1.0: fade = t
        elif t > total_dur - 1.5: fade = max(0.0, (total_dur - t) / 1.5)
        for fi, f in enumerate(freqs):
            val = math.sin(2 * math.pi * f * t) * (0.05 / (1 + fi))
            l[i] += val * fade
            r[i] += val * fade
            
    # Chime at beginning
    for i in range(int(2.5 * SAMPLE_RATE)):
        t = i / SAMPLE_RATE
        env = math.exp(-2.0 * t) * 0.12
        chime = math.sin(2 * math.pi * 880.0 * t) * env
        l[i] += chime
        r[i] += chime
        
    write_wav(pad_wav, l, r)
    
    # 3. Mix voice (with warm delay/reverb) and atmospheric sound into pristine MP3
    filter_complex = (
        "[0:a]adelay=600|600,volume=1.8,aecho=0.8:0.6:60:0.3[v];"
        "[1:a]volume=0.35[bg];"
        "[v][bg]amix=inputs=2:duration=longest:dropout_transition=2[out]"
    )
    subprocess.run([
        'ffmpeg', '-i', flite_wav, '-i', pad_wav,
        '-filter_complex', filter_complex,
        '-map', '[out]',
        '-codec:a', 'libmp3lame', '-b:a', '192k', '-ar', '44100',
        '-y', output_mp3
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    
    # Clean temp files
    if os.path.exists(flite_wav): os.remove(flite_wav)
    if os.path.exists(pad_wav): os.remove(pad_wav)

print("Generating merlin_theme.mp3...")
merlin_wav = "/tmp/merlin_theme.wav"
generate_merlin_theme(merlin_wav, duration=32.0)
subprocess.run([
    'ffmpeg', '-i', merlin_wav,
    '-codec:a', 'libmp3lame', '-b:a', '192k', '-ar', '44100',
    '-id3v2_version', '3',
    '-metadata', 'title=Merlin Theme',
    '-metadata', 'artist=Harmony Introspection',
    '-y', 'app/src/main/res/raw/merlin_theme.mp3'
], check=True)
if os.path.exists(merlin_wav): os.remove(merlin_wav)

print("Generating introspection_color.mp3...")
build_narration(
    "Was ist deine Lieblingsfarbe? Schließe kurz die Augen und spüre die Farbe, die dich am tiefsten berührt.",
    "app/src/main/res/raw/introspection_color.mp3"
)

print("Generating introspection_animal.mp3...")
build_narration(
    "Was ist dein Lieblingstier? Welches Wesen zieht dich magisch an, wenn du an die Natur denkst?",
    "app/src/main/res/raw/introspection_animal.mp3"
)

print("Generating introspection_water.mp3...")
build_narration(
    "Wie nimmst du Wasser wahr? Stelle dir ein Gewässer vor. Wie fühlt es sich für dich an?",
    "app/src/main/res/raw/introspection_water.mp3"
)

print("Generating introspection_reveal.mp3...")
build_narration(
    "Das Portal öffnet sich. Deine inneren Antworten verweben sich mit deiner Essenz. Lausche der Botschaft deiner Seele.",
    "app/src/main/res/raw/introspection_reveal.mp3"
)

print("Audio generation completed successfully!")
