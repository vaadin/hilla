import type { ConnectionIndicator, ConnectionStateStore } from '@vaadin/common-frontend';

interface VaadinRegistration {
  readonly is: string;
  readonly version?: string;
}

interface VaadinFlow {
  readonly clients?: Record<string, unknown>;
}

interface Vaadin {
  readonly Flow?: VaadinFlow;
  readonly connectionIndicator?: ConnectionIndicator;
  readonly connectionState?: ConnectionStateStore;
  registrations?: VaadinRegistration[];
}

export interface VaadinWindow extends Window {
  Vaadin?: Vaadin;
}
