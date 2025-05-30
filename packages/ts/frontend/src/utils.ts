/// <reference lib="webworker" />

// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
export const VAADIN_BROWSER_ENVIRONMENT: boolean = globalThis.document !== undefined;
