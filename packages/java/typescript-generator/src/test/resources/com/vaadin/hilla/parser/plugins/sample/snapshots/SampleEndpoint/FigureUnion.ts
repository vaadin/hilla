import type Blank from './Figure/Blank.js';
import type Ring from './Figure/Ring.js';
import type Round from './Figure/Round.js';

type FigureUnion = (Round & { '@type': 'round' }) | Ring | Blank;

export default FigureUnion;
