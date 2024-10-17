import type { Signal } from '@vaadin/hilla-react-signals';
import type { ViewConfig } from '../types.js';
import type { RouteParamType } from './routeParamType.js';

/**
 * Internal type used for server communication and menu building. It extends the
 * view configuration with the route parameters.
 */
export type ServerViewConfig = Readonly<{
  children?: readonly ServerViewConfig[];
  params?: Readonly<Record<string, RouteParamType>>;
}> &
  ViewConfig;

export type ServerViewMapItem = Readonly<{
  params?: Readonly<Record<string, RouteParamType>>;
}> &
  ViewConfig;

export type VaadinObject = Readonly<{
  views: Readonly<Record<string, ServerViewMapItem>>;
  documentTitleSignal?: Signal;
}>;

export type VaadinWindow = Readonly<{
  Vaadin?: VaadinObject;
}> &
  Window;
