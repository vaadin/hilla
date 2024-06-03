import {computed, type ReadonlySignal, Signal} from "@preact/signals-react";
import type {EntryId} from "./types.js";

declare module "@preact/signals-react" {
  // https://github.com/preactjs/signals/issues/351#issuecomment-1515488634
  // @ts-ignore
  class Signal {
    protected S(node: any): void;
    protected U(node: any): void
  }
}

export class DependencyTrackSignal<T = any> extends Signal<T> {
  private readonly onSubscribe: () => void;
  private readonly onUnsubscribe: () => void;

  private subscribeCount = 0;

  constructor(value: T | undefined, onSubscribe: () => void, onUnsubscribe: () => void) {
    super(value);
    this.onSubscribe = onSubscribe;
    this.onUnsubscribe = onUnsubscribe;
  }

  protected override S(node: any): void {
    super.S(node);
    if (this.subscribeCount++ == 0) {
      this.onSubscribe.call(null);
    }
  }

  protected override U(node: any): void {
    super.U(node);
    if (--this.subscribeCount == 0) {
      this.onUnsubscribe.call(null);
    }
  }
}

declare class Computed<T> extends Signal<T> implements ReadonlySignal<T> {
  constructor(compute: () => T);
}

function Computed <T>(this: Computed<T>, compute: () => T) {
  // Replica of the private Computed constructor
  Signal.call(this, undefined);

  const anyThis = this as any;
  // _compute
  anyThis.x = compute;
  // _sources
  anyThis.s = undefined;
  // _globalVersion
  anyThis.g =  - 1;
  // _flags = OUTDATED
  anyThis.f = 1 << 2;
}

// Create dummy real Computed to be able to get its prototype
Computed.prototype = (computed(() => {}) as any).__proto__;

export abstract class SharedSignal<T> extends Computed<T> {
  override readonly key: EntryId;

  constructor(compute: () => T, key: EntryId) {
    super(compute);
    this.key = key;
  }
}

type EntryReference<S = SharedSignal<any>> = EntryId | S;

interface ListInsertResult<S extends SharedSignal<any>> {
  readonly promise: Promise<boolean>,

  readonly signal: S,
}

export { type ListInsertResult };
