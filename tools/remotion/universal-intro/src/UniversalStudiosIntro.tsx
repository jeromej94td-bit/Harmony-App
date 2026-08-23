import React from 'react';
import {
  AbsoluteFill,
  Easing,
  Img,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from 'remotion';

type IconProps = {size?: number};

const PeopleIcon: React.FC<IconProps> = ({size = 70}) => (
  <svg width={size} height={size} viewBox="0 0 64 64" fill="none">
    <circle cx="24" cy="21" r="9" fill="white" />
    <circle cx="43" cy="24" r="7" fill="white" opacity="0.9" />
    <path d="M8 51c1.5-11 8.5-17 17-17s15.5 6 17 17H8Z" fill="white" />
    <path d="M36 50c1-7 5.5-12 12-12 5 0 9.5 4 11 12H36Z" fill="white" opacity="0.9" />
  </svg>
);

const PaletteIcon: React.FC<IconProps> = ({size = 70}) => (
  <svg width={size} height={size} viewBox="0 0 64 64" fill="none">
    <path d="M32 8C18.7 8 8 17.7 8 30c0 12.2 9.6 22 21.5 22H35c4.2 0 6.2-4.9 3.3-7.9-1.6-1.6-.4-4.2 1.9-4.2H45c7.1 0 11-5.4 11-12C56 16.8 45.8 8 32 8Z" fill="white" />
    <circle cx="20" cy="25" r="4" fill="#39C9C1" />
    <circle cx="29" cy="17" r="4" fill="#39C9C1" />
    <circle cx="41" cy="19" r="4" fill="#39C9C1" />
    <circle cx="17" cy="36" r="4" fill="#39C9C1" />
  </svg>
);

const MovieIcon: React.FC<IconProps> = ({size = 70}) => (
  <svg width={size} height={size} viewBox="0 0 64 64" fill="none">
    <path d="M10 21h44v31H10V21Z" rx="4" fill="white" />
    <path d="M8 13.5 51 7l3 11-43 6.5-3-11Z" fill="white" />
    <path d="m17 12 7-1-5 10-7 1 5-10Zm15-2.3 7-1-5 10-7 1 5-10Zm15-2.2 5-.8 2.4 8.8-2.4 3.6-7 1 5-10.6Z" fill="#6F63F5" />
  </svg>
);

const FlightIcon: React.FC<IconProps> = ({size = 70}) => (
  <svg width={size} height={size} viewBox="0 0 64 64" fill="none">
    <path d="M57 30.5 37 22 36 8c-.1-3-2.1-5-4-5s-3.9 2-4 5l-1 14-20 8.5v6L27 33v14l-7 5v5l12-3 12 3v-5l-7-5V33l20 3.5v-6Z" fill="white" />
  </svg>
);

const RestaurantIcon: React.FC<IconProps> = ({size = 70}) => (
  <svg width={size} height={size} viewBox="0 0 64 64" fill="none">
    <path d="M11 7v20c0 5 3 8 7 9v21h7V36c4-1 7-4 7-9V7h-5v15h-4V7h-5v15h-3V7h-4Z" fill="white" />
    <path d="M43 7c-5 4-8 11-8 20v10h7v20h7V7h-6Z" fill="white" />
  </svg>
);

const cards = [
  {key: 'family', label: 'Familie', accent: '#F28D99', Icon: PeopleIcon},
  {key: 'hobbies', label: 'Hobbys', accent: '#3ED0C6', Icon: PaletteIcon},
  {key: 'movies', label: 'Filme / Serien', accent: '#7568FF', Icon: MovieIcon},
  {key: 'travel', label: 'Reisen', accent: '#45B8FF', Icon: FlightIcon},
  {key: 'food', label: 'Essen & Genuss', accent: '#F59A43', Icon: RestaurantIcon},
] as const;

const fanX = [-178, -88, 0, 88, 178];
const fanY = [28, -18, -42, -18, 28];
const fanRot = [-17, -8.5, 0, 8.5, 17];

const particles = Array.from({length: 34}, (_, index) => ({
  x: (index * 83 + 37) % 700,
  y: (index * 137 + 61) % 1240,
  size: 2 + (index % 4),
  phase: (index * 11) % 30,
}));

const hearts = [
  {x: 45, y: 170, size: 34, rot: -18},
  {x: 650, y: 220, size: 28, rot: 16},
  {x: 92, y: 760, size: 24, rot: 13},
  {x: 620, y: 860, size: 36, rot: -12},
  {x: 34, y: 1070, size: 27, rot: -18},
  {x: 650, y: 1120, size: 24, rot: 18},
];

const HarmonyBackground: React.FC = () => {
  const frame = useCurrentFrame();
  const {fps} = useVideoConfig();
  const drift = Math.sin(frame / (fps * 1.4));

  return (
    <AbsoluteFill
      style={{
        overflow: 'hidden',
        background:
          'radial-gradient(circle at 18% 18%, rgba(255,53,139,.32), transparent 31%), radial-gradient(circle at 82% 32%, rgba(83,114,255,.30), transparent 34%), radial-gradient(circle at 48% 78%, rgba(151,64,210,.22), transparent 39%), linear-gradient(180deg,#240728 0%,#14051E 48%,#08010F 100%)',
      }}
    >
      <div
        style={{
          position: 'absolute',
          width: 650,
          height: 650,
          left: 35 + drift * 18,
          top: 265,
          borderRadius: '50%',
          border: '2px solid rgba(255,126,196,.14)',
          boxShadow: '0 0 90px rgba(137,67,226,.12) inset',
        }}
      />
      <div
        style={{
          position: 'absolute',
          width: 510,
          height: 510,
          left: 145 - drift * 25,
          top: 420,
          borderRadius: '50%',
          border: '2px solid rgba(80,207,255,.10)',
        }}
      />
      {particles.map((particle, index) => {
        const pulse = 0.32 + 0.68 * Math.abs(Math.sin((frame + particle.phase) / 24));
        return (
          <div
            key={index}
            style={{
              position: 'absolute',
              left: particle.x,
              top: particle.y + Math.sin((frame + index * 8) / 40) * 9,
              width: particle.size,
              height: particle.size,
              borderRadius: '50%',
              background: index % 3 === 0 ? '#FF7CCB' : index % 3 === 1 ? '#7FDFFF' : '#FFFFFF',
              opacity: pulse * 0.68,
              boxShadow: '0 0 10px currentColor',
            }}
          />
        );
      })}
      {hearts.map((heart, index) => (
        <div
          key={index}
          style={{
            position: 'absolute',
            left: heart.x + Math.sin((frame + index * 20) / 35) * 8,
            top: heart.y + Math.cos((frame + index * 16) / 42) * 11,
            fontSize: heart.size,
            lineHeight: 1,
            color: index % 2 ? 'rgba(226,92,255,.34)' : 'rgba(255,74,159,.42)',
            rotate: `${heart.rot + Math.sin(frame / 50) * 4}deg`,
            textShadow: '0 0 14px rgba(255,74,159,.45)',
          }}
        >
          ♥
        </div>
      ))}
    </AbsoluteFill>
  );
};

type CardProps = {
  index: number;
  focus: number;
  intro: number;
};

const CategoryCard: React.FC<CardProps> = ({index, focus, intro}) => {
  const frame = useCurrentFrame();
  const card = cards[index];
  const isMovie = card.key === 'movies';
  const Icon = card.Icon;

  const baseX = fanX[index] * intro;
  const baseY = interpolate(intro, [0, 1], [210 + index * 14, fanY[index]], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const baseRot = interpolate(intro, [0, 1], [index % 2 === 0 ? -4 : 4, fanRot[index]], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  const exitDirection = index < 2 ? -1 : 1;
  const nonMovieOpacity = isMovie
    ? 1
    : interpolate(focus, [0, 0.68, 1], [1, 0.72, 0], {
        extrapolateLeft: 'clamp',
        extrapolateRight: 'clamp',
      });

  const width = isMovie ? interpolate(focus, [0, 1], [250, 592]) : 250;
  const height = isMovie ? interpolate(focus, [0, 1], [348, 500]) : 348;
  const x = isMovie
    ? interpolate(focus, [0, 1], [baseX, 0])
    : baseX + focus * exitDirection * (280 + index * 45);
  const y = isMovie
    ? interpolate(focus, [0, 1], [baseY, 45])
    : baseY + focus * 110;
  const rotation = isMovie ? interpolate(focus, [0, 1], [baseRot, 0]) : baseRot + focus * exitDirection * 14;
  const scale = isMovie ? 1 : 1 - focus * 0.08;
  const labelOpacity = isMovie ? interpolate(focus, [0.3, 0.75], [0.35, 1], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'}) : 0.9;
  const finalTextOpacity = isMovie
    ? interpolate(focus, [0.56, 0.88], [0, 1], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'})
    : 0;
  const iconSize = isMovie ? interpolate(focus, [0, 1], [72, 104]) : 72;
  const glow = 0.6 + Math.sin(frame / 18 + index) * 0.18;

  return (
    <div
      style={{
        position: 'absolute',
        left: '50%',
        top: '54%',
        width,
        height,
        marginLeft: -width / 2,
        marginTop: -height / 2,
        translate: `${x}px ${y}px`,
        rotate: `${rotation}deg`,
        scale,
        opacity: nonMovieOpacity,
        borderRadius: isMovie ? interpolate(focus, [0, 1], [32, 42]) : 32,
        overflow: 'hidden',
        background: `radial-gradient(circle at 30% 18%, ${card.accent}55, transparent 38%), linear-gradient(145deg, ${card.accent}44 0%, rgba(47,20,61,.97) 46%, rgba(15,5,25,.99) 100%)`,
        border: `3px solid ${card.accent}`,
        boxShadow: `0 18px 55px rgba(0,0,0,.42), 0 0 ${24 + glow * 18}px ${card.accent}55, inset 0 0 38px rgba(255,255,255,.045)`,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
      }}
    >
      <div
        style={{
          position: 'absolute',
          inset: 18,
          borderRadius: 25,
          border: `1px solid ${card.accent}55`,
        }}
      />
      <div
        style={{
          width: isMovie ? interpolate(focus, [0, 1], [116, 160]) : 116,
          height: isMovie ? interpolate(focus, [0, 1], [116, 160]) : 116,
          borderRadius: '50%',
          background: `radial-gradient(circle, ${card.accent}77 0%, ${card.accent}33 55%, rgba(255,255,255,.04) 100%)`,
          border: `3px solid ${card.accent}`,
          boxShadow: `0 0 38px ${card.accent}88`,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: isMovie ? interpolate(focus, [0, 1], [24, 32]) : 24,
        }}
      >
        <Icon size={iconSize} />
      </div>
      <div
        style={{
          color: '#FFFFFF',
          opacity: labelOpacity,
          fontFamily: 'Arial, Helvetica, sans-serif',
          fontSize: isMovie ? interpolate(focus, [0, 1], [24, 38]) : 24,
          fontWeight: 800,
          letterSpacing: isMovie ? interpolate(focus, [0, 1], [0.2, 1.2]) : 0.2,
          textAlign: 'center',
          textShadow: '0 3px 12px rgba(0,0,0,.35)',
        }}
      >
        {card.label}
      </div>
      {isMovie ? (
        <div
          style={{
            opacity: finalTextOpacity,
            marginTop: 22,
            padding: '10px 20px',
            borderRadius: 22,
            color: '#D9D4FF',
            background: 'rgba(118,104,255,.15)',
            border: '1px solid rgba(165,151,255,.35)',
            fontFamily: 'Arial, Helvetica, sans-serif',
            fontSize: 20,
            fontWeight: 700,
            letterSpacing: 1.1,
          }}
        >
          UNIVERSAL STUDIOS
        </div>
      ) : null}
      <div
        style={{
          position: 'absolute',
          width: 260,
          height: 90,
          rotate: '-22deg',
          top: -45,
          left: -110 + ((frame * 5 + index * 80) % 620),
          background: 'linear-gradient(90deg, transparent, rgba(255,255,255,.11), transparent)',
          opacity: 0.55,
        }}
      />
    </div>
  );
};

export const UniversalStudiosIntro: React.FC = () => {
  const frame = useCurrentFrame();
  const {fps} = useVideoConfig();

  const intro = spring({
    frame: frame - 16,
    fps,
    config: {damping: 15, stiffness: 78, mass: 0.9},
    durationInFrames: 62,
  });
  const focus = interpolate(frame, [118, 169], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.16, 1, 0.3, 1),
  });
  const introOpacity = interpolate(frame, [116, 148], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const finalFade = interpolate(frame, [192, 209], [1, 0.08], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
    easing: Easing.bezier(0.4, 0, 0.2, 1),
  });

  return (
    <AbsoluteFill style={{backgroundColor: '#09010F'}}>
      <HarmonyBackground />

      <div
        style={{
          position: 'absolute',
          top: 78,
          width: '100%',
          textAlign: 'center',
          opacity: introOpacity,
          fontFamily: 'Arial, Helvetica, sans-serif',
        }}
      >
        <div
          style={{
            fontSize: 43,
            fontWeight: 900,
            letterSpacing: 7,
            background: 'linear-gradient(90deg,#FF4F9C,#B25CFF,#64C9FF)',
            WebkitBackgroundClip: 'text',
            color: 'transparent',
            textShadow: '0 0 24px rgba(255,79,156,.18)',
          }}
        >
          HARMONY
        </div>
        <div
          style={{
            marginTop: 12,
            fontSize: 17,
            fontWeight: 700,
            color: 'rgba(255,255,255,.72)',
            letterSpacing: 2.4,
          }}
        >
          DEIN THEMA · DEINE FRAGEN
        </div>
      </div>

      <Img
        src={staticFile('panda.png')}
        style={{
          position: 'absolute',
          width: 350,
          height: 350,
          objectFit: 'contain',
          left: 185,
          top: 250 + Math.sin(frame / 24) * 7,
          opacity: introOpacity,
          scale: interpolate(intro, [0, 1], [0.9, 1], {extrapolateLeft: 'clamp', extrapolateRight: 'clamp'}),
          filter: 'drop-shadow(0 22px 28px rgba(0,0,0,.42)) drop-shadow(0 0 24px rgba(182,93,255,.22))',
        }}
      />

      <div style={{opacity: finalFade}}>
        {cards.map((card, index) => (
          <CategoryCard key={card.key} index={index} focus={focus} intro={intro} />
        ))}
      </div>

      <div
        style={{
          position: 'absolute',
          left: 0,
          right: 0,
          bottom: 76,
          textAlign: 'center',
          color: 'rgba(255,255,255,.54)',
          fontFamily: 'Arial, Helvetica, sans-serif',
          fontSize: 15,
          fontWeight: 700,
          letterSpacing: 1.4,
          opacity: interpolate(frame, [58, 85, 122, 145], [0, 1, 1, 0], {
            extrapolateLeft: 'clamp',
            extrapolateRight: 'clamp',
          }),
        }}
      >
        WÄHLE DEINE WELT
      </div>

      <AbsoluteFill
        style={{
          pointerEvents: 'none',
          opacity: interpolate(frame, [200, 209], [0, 0.42], {
            extrapolateLeft: 'clamp',
            extrapolateRight: 'clamp',
          }),
          background: 'linear-gradient(180deg,rgba(20,4,28,0),rgba(7,1,12,.78))',
        }}
      />
    </AbsoluteFill>
  );
};
