import type OtherSubtype from './MinimalBase/OtherSubtype.js';
import type Subtype from './MinimalBase/Subtype.js';

type MinimalBaseUnion = Subtype | OtherSubtype;

export default MinimalBaseUnion;
