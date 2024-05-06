import type { ViewConfig } from '../types.js';

export type VaadinObject = Readonly<{
  views: Readonly<Record<string, ViewConfig>>;
}>;

export type VaadinWindow = Readonly<{
  Vaadin?: VaadinObject;
}> &
  Window;
