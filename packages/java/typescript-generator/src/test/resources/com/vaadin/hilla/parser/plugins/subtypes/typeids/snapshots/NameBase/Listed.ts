import type NameBase from '../NameBase.js';

interface Listed extends NameBase {
  tag?: string;
  '@type': 'listed';
}

export default Listed;
