import type EnumLookupValue from './EnumLookupValue.js';
import type NodeType from './NodeType.js';

interface NarrowedLookup {
  value?: EnumLookupValue<NodeType | undefined>;
}

export default NarrowedLookup;
