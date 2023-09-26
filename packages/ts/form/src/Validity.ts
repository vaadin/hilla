/**
 * Symbol for storing `ValidityState` in the binder nodes.
 */
export const _validity = Symbol('validity');

/**
 * Default validity state with `valid` flag set, assuming a valid state.
 */
export const defaultValidity: ValidityState = {
  badInput: false,
  customError: false,
  patternMismatch: false,
  rangeOverflow: false,
  rangeUnderflow: false,
  stepMismatch: false,
  tooLong: false,
  tooShort: false,
  typeMismatch: false,
  valueMissing: false,
  valid: true,
};
