import React from 'react';
import {Composition} from 'remotion';
import {UniversalStudiosIntro} from './UniversalStudiosIntro';

export const RemotionRoot: React.FC = () => {
  return (
    <Composition
      id="UniversalStudiosIntro"
      component={UniversalStudiosIntro}
      durationInFrames={210}
      fps={30}
      width={720}
      height={1280}
    />
  );
};
