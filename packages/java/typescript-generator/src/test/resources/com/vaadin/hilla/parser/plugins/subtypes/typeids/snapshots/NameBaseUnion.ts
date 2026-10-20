import type Aliased from './NameBase/Aliased.js';
import type Listed from './NameBase/Listed.js';
import type Named from './NameBase/Named.js';
import type Nested from './NameBase/Nested.js';

type NameBaseUnion = Listed | Named | Nested | Aliased;

export default NameBaseUnion;
