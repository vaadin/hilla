declare global {
  interface Window {
    Vaadin: {
      featureFlags?: {
        fullstackSignals: boolean;
      };
    };
  }
}

window.Vaadin = {
  featureFlags: {
    fullstackSignals: true,
  },
};

export {};
