import type { CSSProperties } from 'react';

export type ComponentStyleProps = Readonly<{
  id?: string;
  style?: CSSProperties;
  className?: string;
}>;
