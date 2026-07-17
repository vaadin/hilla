/**
 * Minimal shape of the connection state store that Vaadin Flow exposes on the
 * global `window.Vaadin` object. Hilla only reports loading progress to it; the
 * store itself is provided by Flow at runtime (it is not created by Hilla).
 */
export interface ConnectionStateStore {
  loadingStarted(): void;
  loadingFinished(): void;
  loadingFailed(): void;
}

export interface VaadinRegistration {
  readonly is: string;
  readonly version?: string;
}

export interface VaadinFlow {
  readonly clients?: Record<string, unknown>;
}

export interface Vaadin {
  readonly Flow?: VaadinFlow;
  readonly connectionState?: ConnectionStateStore;
  registrations?: VaadinRegistration[];
}

export interface VaadinGlobal {
  Vaadin?: Vaadin;
}
