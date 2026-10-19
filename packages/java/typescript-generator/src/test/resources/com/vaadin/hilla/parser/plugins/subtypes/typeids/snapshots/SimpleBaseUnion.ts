import type FromInterface from './SimpleBase/FromInterface.js';
import type FromSuperclass from './SimpleBase/FromSuperclass.js';
import type Nested from './SimpleBase/Nested.js';

type SimpleBaseUnion = Nested | FromInterface | FromSuperclass;

export default SimpleBaseUnion;
