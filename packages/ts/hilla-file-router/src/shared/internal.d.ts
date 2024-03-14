import type { ViewConfig } from '../types.js';
import type { RouteParamType } from './routeParamType.js';

/**
 * Internal type used for server communication and menu building. It extends the
 * view configuration with the route parameters.
 */
export type ServerViewConfig = Readonly<{
  children: readonly ServerViewConfig[];
  params?: Readonly<Record<string, RouteParamType>>;
}> &
  ViewConfig;

export type ServerViewMapItem = Readonly<{
  params?: Readonly<Record<string, RouteParamType>>;
}> &
  ViewConfig;

export type VaadinServer = Readonly<{
  views: Readonly<Record<string, ServerViewMapItem>>;
}>;

export type VaadinObject = Readonly<{
  server?: VaadinServer;
}>;

export type VaadinWindow = Readonly<{
  Vaadin?: VaadinObject;
}> &
  Window;
