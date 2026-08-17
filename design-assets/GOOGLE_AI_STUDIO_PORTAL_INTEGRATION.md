# Google AI Studio – animiertes Unterbewusstsein-Portal

Verbindliche Figma-Quelle: https://www.figma.com/design/XWp4LnXSTjk3nQvoDbYD88

## Quelldatei

`design-assets/introspection_portal_animated.svg`

Die SVG ist frei skalierbar und enthält:

- pulsierende Portal-Atmung;
- gegenläufig rotierende Energiebögen;
- flimmernde Aura;
- schwebende Partikel mit individuellen Phasen;
- pulsierende Bodenreflexion;
- Sternpuls;
- `prefers-reduced-motion`-Fallback.

## Verbindlicher Auftrag an Google AI Studio

Nutze die SVG als visuelle und geometrische Quelle für `IntrospectionPortal.kt`. Implementiere die Animation für Android nativ mit Jetpack Compose Canvas, `rememberInfiniteTransition`, `Animatable`, `drawWithCache`, `graphicsLayer`, `Brush.radialGradient` und `Brush.sweepGradient`. Rasterisiere die SVG nicht und verwende keine WebView. So bleiben Auflösung, Performance und Reduced-Motion-Unterstützung auf allen Android-Geräten erhalten.

Die SVG-CSS-Werte sind verbindlich:

- Portal-Atmung: Scale `0.965 → 1.035 → 0.965`, 3.200 ms, EaseInOut, endlos;
- äußerer Ring: 360° in 9.000 ms linear;
- innerer Ring: −360° in 13.000 ms linear;
- Aura: Alpha `0.42 → 0.72 → 0.48`, 2.350 ms;
- Reflexion: ScaleX `0.92 → 1.08`, Alpha `0.18 → 0.34`, 2.800 ms, 180-ms-Versatz;
- Partikel: 4.400–7.100 ms mit stabil gespeicherten Startphasen;
- Reduced Motion: keine Ringrotation, Scale nur `0.99 ↔ 1.01`.

Während `Enthüllung.mp3` wird der Glow um 25 % intensiviert und die Partikelgeschwindigkeit um 18 % erhöht. Während einer Audioaufnahme pausiert die Hintergrundmusik, das Portal bleibt sichtbar und der Aufnahmering pulsiert mit 1.100 ms.

Der Name der Hintergrundmusik darf nirgendwo in der Benutzeroberfläche erscheinen. Die Emojis `🧙‍♂️` und `✨️` bleiben exakt unverändert.
