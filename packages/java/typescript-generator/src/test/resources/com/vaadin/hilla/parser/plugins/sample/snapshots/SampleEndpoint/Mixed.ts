import type { Signal } from '@vaadin/hilla-react-signals';
import type Kind from './Kind.js';
import type Sample from './Sample.js';

interface Mixed {
  count: number;
  active: boolean;
  tags?: Array<string | undefined>;
  counts?: Record<string, number | undefined>;
  kind?: Kind;
  sample?: Sample;
  words?: Array<string | undefined>;
  unique?: Array<string | undefined>;
  grouped?: Record<string, Array<string | undefined> | undefined>;
  maybeTags?: Array<string | undefined>;
  signal?: Signal<string | undefined>;
}

export default Mixed;
