import { isPage, isSlice, enhancePage, enhanceSlice } from './page.js';

export function reviver(_key: string, value: unknown): unknown {
  // Convert null to undefined
  if (value === null) {
    return undefined;
  }

  // Enhance Page objects with array-like access
  if (isPage(value)) {
    return enhancePage(value);
  }

  // Enhance Slice objects with array-like access
  if (isSlice(value)) {
    return enhanceSlice(value);
  }

  return value;
}
