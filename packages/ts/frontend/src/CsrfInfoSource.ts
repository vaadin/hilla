/// <reference lib="webworker" />
// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
const csrfUtilsPromise = globalThis.document ? import('./CsrfUtils.js') : undefined;

/** @internal */
export type NameValueEntry = readonly [name: string, name: string];
/** @internal */
export type CsrfInfo = {
  readonly headerEntries: readonly NameValueEntry[];
  readonly formDataEntries: readonly NameValueEntry[];
};

function isCsrfInfo(o: unknown): o is CsrfInfo {
  return o !== null && typeof o === 'object' && 'headerEntries' in o && 'formDataEntries' in o;
}

/** @internal */
export interface CsrfInfoSource {
  get(): Promise<CsrfInfo>;
  open(): void;
  close(): void;
}

class SharedCsrfInfoSource implements CsrfInfoSource {
  #updateChannel: BroadcastChannel | undefined;
  #requestUpdateChannel: BroadcastChannel | undefined;
  #valuePromise: Promise<CsrfInfo>;
  #resolveInitialValue?: (csrfInfo: CsrfInfo) => void;
  #requestUpdateResponded: boolean = false;

  constructor() {
    this.#valuePromise = this._getInitialValue();
    this.open();
  }

  open() {
    this.#updateChannel = new BroadcastChannel(this.#getBroadcastChannelName('update'));
    this.#updateChannel.onmessage = (e: MessageEvent) => {
      if (!isCsrfInfo(e.data)) {
        return;
      }
      this.#requestUpdateResponded = true;
      this.#receiveCsrfInfo(e.data);
      if ('waitUntil' in e) {
        (e as ExtendableMessageEvent).waitUntil(this.#valuePromise);
      }
    };

    this.#requestUpdateChannel = new BroadcastChannel(this.#getBroadcastChannelName('requestUpdate'));
    this.#requestUpdateChannel.onmessage = (e: MessageEvent) => {
      this.#requestUpdateResponded = false;
      const promise = this.get().then((csrfInfo: CsrfInfo) => {
        if (!this.#requestUpdateResponded) {
          this.#sendCsrfInfo(csrfInfo);
        }
      });
      if ('waitUntil' in e) {
        (e as ExtendableMessageEvent).waitUntil(promise);
      }
    };

    if (this.#resolveInitialValue) {
      this.#requestUpdateChannel.postMessage(undefined);
    } else {
      this.#valuePromise.then((csrfInfo: CsrfInfo) => {
        this.#sendCsrfInfo(csrfInfo);
      }, console.error);
    }
  }

  close(): void {
    if (this.#requestUpdateChannel) {
      this.#requestUpdateChannel.onmessage = null;
      this.#requestUpdateChannel.close();
      this.#requestUpdateChannel = undefined;
    }

    if (this.#updateChannel) {
      this.#updateChannel.onmessage = null;
      this.#updateChannel.close();
      this.#updateChannel = undefined;
    }
  }

  #getBroadcastChannelName(name: string): string {
    return `@vaadin/hilla-frontend/SharedCsrfUtils.${name}`;
  }

  async get(): Promise<CsrfInfo> {
    return this.#valuePromise;
  }

  protected async _getInitialValue(): Promise<CsrfInfo> {
    return new Promise<CsrfInfo>((resolve) => {
      this.#resolveInitialValue = resolve;
    });
  }

  #sendCsrfInfo(csrfInfo: CsrfInfo) {
    if (this.#updateChannel) {
      this.#updateChannel.postMessage(csrfInfo);
    }
  }

  #receiveCsrfInfo(csrfInfo: CsrfInfo) {
    if (this.#resolveInitialValue) {
      this.#resolveInitialValue(csrfInfo);
      this.#resolveInitialValue = undefined;
    } else {
      this.#valuePromise = Promise.resolve(csrfInfo);
    }
  }
}

export async function extractCsrfInfoFromDocument(document: Document): Promise<CsrfInfo> {
  const csrfUtils = await csrfUtilsPromise!;
  const headerEntries: NameValueEntry[] = [];
  const formDataEntries: NameValueEntry[] = [];
  const springCsrfInfo = csrfUtils.getSpringCsrfInfo(document);
  if (springCsrfInfo._csrf && springCsrfInfo._csrf_header) {
    headerEntries.push([springCsrfInfo._csrf_header, springCsrfInfo._csrf]);
  } else {
    headerEntries.push([csrfUtils.VAADIN_CSRF_HEADER, csrfUtils.getVaadinCsrfToken()]);
  }
  if (springCsrfInfo._csrf && springCsrfInfo._csrf_parameter) {
    formDataEntries.push([springCsrfInfo._csrf_parameter, springCsrfInfo._csrf]);
  }
  return { headerEntries, formDataEntries };
}

class BrowserCsrfInfoSource extends SharedCsrfInfoSource {
  constructor() {
    super();
    globalThis.addEventListener('pagehide', this.close.bind(this));
    globalThis.addEventListener('pageshow', this.open.bind(this));
  }

  protected override async _getInitialValue(): Promise<CsrfInfo> {
    return extractCsrfInfoFromDocument(globalThis.document);
  }
}

/** @internal */
// eslint-disable-next-line import/no-mutable-exports
let defaultCsrfInfoSource: CsrfInfoSource;
// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
if (globalThis.document) {
  defaultCsrfInfoSource = new BrowserCsrfInfoSource();
} else {
  defaultCsrfInfoSource = new SharedCsrfInfoSource();
}
export default defaultCsrfInfoSource;
