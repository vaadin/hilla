import type Pet from './Pet.js';

interface Cat extends Pet {
  indoor: boolean;
  '@type': 'cat';
}

export default Cat;
