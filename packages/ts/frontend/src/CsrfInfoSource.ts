/// <reference lib="webworker" />
// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
const csrfUtilsPromise = globalThis.document ? import('./CsrfUtils.js') : undefined;

/** @internal */
export type NameValueEntry = readonly [name: string, name: string];
/** @internal */
export type CsrfInfo = {
  readonly headerEntries: readonly NameValueEntry[];
  readonly formDataEntries: readonly NameValueEntry[];
  readonly timestamp: number;
};

function isCsrfInfo(o: unknown): o is CsrfInfo {
  return o !== null && typeof o === 'object' && 'headerEntries' in o && 'formDataEntries' in o;
}

/**
 * The source of CSRF related headers and parameters for endpoint requests.
 *
 * @internal may change or be removed in a future version.
 **/
export interface CsrfInfoSource {
  /**
   * Get an up-to-date value.
   */
  get(): Promise<CsrfInfo>;

  /**
   * Reset back to initial value and re-initialize.
   */
  reset(): void;

  /**
   * Close internal message channels. May be called to free resources up
   * whenever the value is not needed.
   */
  close(): void;

  /**
   * Reopen internal message channels after prior closing and request a shared
   * value from another clients.
   */
  open(): void;
}

/** @internal */
export class SharedCsrfInfoSource implements CsrfInfoSource {
  #updateChannel: BroadcastChannel | undefined;
  #requestUpdateChannel: BroadcastChannel | undefined;
  #valuePromise!: Promise<CsrfInfo>;
  #resolveInitialValue?: (csrfInfo: CsrfInfo) => void;
  #lastUpdateTimestamp: number = 0;

  constructor() {
    this.reset();
  }

  open(): void {
    const reopen = !!this.#updateChannel && !!this.#requestUpdateChannel;
    if (reopen) {
      this.close();
    }

    this.#updateChannel = new BroadcastChannel(this.#getBroadcastChannelName('update'));
    this.#updateChannel.onmessage = (e: MessageEvent) => {
      if (!isCsrfInfo(e.data)) {
        return;
      }
      const csrfInfo: CsrfInfo = e.data;
      if (csrfInfo.timestamp > this.#lastUpdateTimestamp) {
        this.#lastUpdateTimestamp = csrfInfo.timestamp;
        this.#receiveCsrfInfo(csrfInfo);
      }
    };

    this.#requestUpdateChannel = new BroadcastChannel(this.#getBroadcastChannelName('requestUpdate'));
    this.#requestUpdateChannel.onmessage = () => {
      this.get().then((csrfInfo: CsrfInfo) => {
        this.#sendCsrfInfo(csrfInfo);
      }, console.error);
    };

    if (this.#resolveInitialValue || reopen) {
      this.#requestUpdateChannel.postMessage(undefined);
    }
  }

  close(): void {
    if (this.#requestUpdateChannel?.onmessage) {
      this.#requestUpdateChannel.onmessage = null;
      this.#requestUpdateChannel.close();
    }

    if (this.#updateChannel?.onmessage) {
      this.#updateChannel.onmessage = null;
      this.#updateChannel.close();
    }
  }

  #getBroadcastChannelName(name: string): string {
    return `@vaadin/hilla-frontend/SharedCsrfUtils.${name}`;
  }

  async get(): Promise<CsrfInfo> {
    return this.#valuePromise;
  }

  reset(): void {
    this.close();
    // Remove message channel instances to make sure the following open()
    // works from a clean state instead of reopening.
    this.#updateChannel = undefined;
    this.#requestUpdateChannel = undefined;
    this.#valuePromise = this._getInitial();
    this.open();
    if (!this.#resolveInitialValue) {
      this.get()
        .then((csrfInfo: CsrfInfo) => {
          this.#sendCsrfInfo(csrfInfo);
        })
        .catch(console.error);
    }
  }

  /**
   * Provides initial value for both constructor and `reset()`. The default
   * implementation uses messages to get a shared value from another window
   * or worker client.
   */
  protected async _getInitial(): Promise<CsrfInfo> {
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

/** @internal */
export async function extractCsrfInfoFromMeta(document: Document): Promise<CsrfInfo> {
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
  return { headerEntries, formDataEntries, timestamp: Date.now() };
}

/** @internal */
export class BrowserCsrfInfoSource extends SharedCsrfInfoSource {
  constructor() {
    super();
    globalThis.addEventListener('pagehide', this.close.bind(this));
    globalThis.addEventListener('pageshow', this.open.bind(this));
  }

  protected override async _getInitial(): Promise<CsrfInfo> {
    return extractCsrfInfoFromMeta(globalThis.document);
  }
}

/** @internal */
// eslint-disable-next-line import/no-mutable-exports
let csrfInfoSource: CsrfInfoSource;
// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
if (globalThis.document) {
  csrfInfoSource = new BrowserCsrfInfoSource();
} else {
  csrfInfoSource = new SharedCsrfInfoSource();
}
export default csrfInfoSource;
