import type Box from './Box.js';

interface Holder {
  boxOfMaybe: Box<string | undefined>;
  maybeBoxOf?: Box<string>;
}

export default Holder;
