import type Figure from '../Figure.js';

interface Round extends Figure {
  radius: number;
  '@type': 'round' | 'ring';
}

export default Round;
