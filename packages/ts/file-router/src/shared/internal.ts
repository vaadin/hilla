import type { ViewConfig } from '../types.js';
import type { RouteParamType } from './routeParamType.js';

const $serverViewConfig: unique symbol = Symbol('serverViewConfig');

export function isServerViewConfig(value: unknown): value is ServerViewConfig {
  return value !== null && typeof value === 'object' && 'brand' in value && value.brand === $serverViewConfig;
}

export function brandServerViewConfig(config: Omit<ServerViewConfig, 'brand'>): ServerViewConfig {
  return { ...config, brand: $serverViewConfig };
}

/**
 * Internal type used for server communication and menu building. It extends the
 * view configuration with the route parameters.
 */
export type ServerViewConfig = Readonly<{
  brand: typeof $serverViewConfig;
  children?: readonly ServerViewConfig[];
  params?: Readonly<Record<string, RouteParamType>>;
}> &
  ViewConfig;

export type VaadinObject = Readonly<{
  views: Readonly<Record<string, ViewConfig>>;
}>;

export type VaadinWindow = Readonly<{
  Vaadin?: VaadinObject;
}> &
  Window;
