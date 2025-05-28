/// <reference lib="webworker" />
// eslint-disable-next-line @typescript-eslint/no-unnecessary-condition
const cookieManagerPromise = globalThis.document ? import('./CookieManager.js') : undefined;

/** @internal */
export type NameValueEntry = readonly [name: string, value: string];
export enum CsrfInfoType {
  SPRING = 'Spring',
  VAADIN = 'Vaadin',
}

/** @internal */
export type CsrfInfo = {
  readonly headerEntries: readonly NameValueEntry[];
  readonly formDataEntries: readonly NameValueEntry[];
  readonly type: CsrfInfoType;
  readonly timestamp: number;
};

function isCsrfInfo(o: unknown): o is CsrfInfo {
  return o !== null && typeof o === 'object' && 'headerEntries' in o && 'formDataEntries' in o && 'timestamp' in o;
}

/** @internal */
export const VAADIN_CSRF_HEADER = 'X-CSRF-Token';
/** @internal */
export const VAADIN_CSRF_COOKIE_NAME = 'csrfToken';
/** @internal */
export const SPRING_CSRF_COOKIE_NAME = 'XSRF-TOKEN';

function extractContentFromMetaTag(doc: Document, metaTag: string): string | undefined {
  const element = doc.head.querySelector<HTMLMetaElement>(`meta[name="${metaTag}"]`);
  const value = element?.content;
  if (value && value.toLowerCase() !== 'undefined') {
    return value;
  }

  return undefined;
}

function updateMetaTag(doc: Document, name: string, content: string): void {
  const meta = doc.createElement('meta');
  meta.name = name;
  meta.content = content;
  const existing = doc.head.querySelector(`meta[name="${name}"]`);
  if (existing) {
    existing.replaceWith(meta);
  } else {
    doc.head.appendChild(meta);
  }
}

export function clearCsrfInfoMeta(doc: Document): void {
  Array.from(
    doc.head.querySelectorAll('meta[name="_csrf"], meta[name="_csrf_header"], meta[name="_csrf_parameter"]'),
  ).forEach((el) => el.remove());
}

export function updateCsrfInfoMeta(csrfInfo: CsrfInfo, doc: Document): void {
  if (csrfInfo.type !== CsrfInfoType.SPRING) {
    return;
  }

  if (csrfInfo.headerEntries.length > 0) {
    const [[csrfHeader, csrf]] = csrfInfo.headerEntries;
    updateMetaTag(doc, '_csrf_header', csrfHeader);
    updateMetaTag(doc, '_csrf', csrf);
  }

  if (csrfInfo.formDataEntries.length > 0) {
    const [[csrfParameter]] = csrfInfo.formDataEntries;
    updateMetaTag(doc, '_csrf_parameter', csrfParameter);
  }
}

/** @internal */
export async function extractCsrfInfoFromMeta(doc: Document): Promise<CsrfInfo> {
  const cookieManager = (await cookieManagerPromise!).default;
  const timestamp = Date.now();
  const springCsrf = cookieManager.get(SPRING_CSRF_COOKIE_NAME) ?? extractContentFromMetaTag(doc, '_csrf');
  if (springCsrf) {
    const csrfHeader = extractContentFromMetaTag(doc, '_csrf_header');
    const csrfParameter = extractContentFromMetaTag(doc, '_csrf_parameter');
    return {
      headerEntries: csrfHeader ? [[csrfHeader, springCsrf]] : [],
      formDataEntries: csrfParameter ? [[csrfParameter, springCsrf]] : [],
      timestamp,
      type: CsrfInfoType.SPRING,
    };
  }

  const vaadinCsrf = cookieManager.get(VAADIN_CSRF_COOKIE_NAME) ?? '';
  return {
    type: CsrfInfoType.VAADIN,
    headerEntries: [[VAADIN_CSRF_HEADER, vaadinCsrf]],
    formDataEntries: [],
    timestamp,
  };
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
    if (this.#updateChannel || this.#requestUpdateChannel) {
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

    // Request an update from peer clients on reopen
    if (this.#lastUpdateTimestamp > 0) {
      this.#requestUpdateChannel.postMessage(undefined);
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

  reset(): void {
    this.#lastUpdateTimestamp = 0;
    this.close();
    this.open();
    this.#valuePromise = this._getInitial().then((csrfInfo: CsrfInfo) => {
      this.#lastUpdateTimestamp = csrfInfo.timestamp;
      return csrfInfo;
    });
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
      this.#requestUpdateChannel?.postMessage(undefined);
    });
  }

  #sendCsrfInfo(csrfInfo: CsrfInfo) {
    this.#updateChannel?.postMessage(csrfInfo);
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
