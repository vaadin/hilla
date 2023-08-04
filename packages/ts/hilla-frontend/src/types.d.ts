import type { ConnectionIndicator, ConnectionStateStore } from '@vaadin/common-frontend';

declare global {
  interface VaadinRegistration {
    is: string;
    version?: string;
  }

  interface VaadinFlow {
    clients?: Record<string, unknown>;
  }

  interface Vaadin {
    Flow?: VaadinFlow;
    connectionIndicator?: ConnectionIndicator;
    connectionState?: ConnectionStateStore;
    registrations?: VaadinRegistration[];
  }

  interface Window {
    Vaadin?: Vaadin;
  }
}
