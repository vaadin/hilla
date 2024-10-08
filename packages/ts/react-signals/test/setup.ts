declare global {
  interface Window {
    Vaadin: {
      featureFlags?: {
        fullstackSignals: boolean;
      };
    };
  }
}

before(() => {
  window.Vaadin = {
    featureFlags: {
      fullstackSignals: true,
    },
  };
});

export {};
