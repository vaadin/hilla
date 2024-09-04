import { Signal as _Signal } from '@preact/signals-react';

declare module './core.js' {
  export declare class Signal<T> extends _Signal<T> {
    protected S(node: unknown): void;
    protected U(node: unknown): void;
  }
}

export {};
