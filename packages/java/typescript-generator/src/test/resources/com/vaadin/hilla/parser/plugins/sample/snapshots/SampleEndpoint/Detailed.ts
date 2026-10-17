import type Sample from './Sample.js';

interface Detailed extends Sample {
  note?: string;
  basis?: Sample;
}

export default Detailed;
