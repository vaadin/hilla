import type Round from './Round.js';

interface Ring extends Round {
  hole: number;
  '@type': 'ring';
}

export default Ring;
