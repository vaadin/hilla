/**
 * A converter for a string representation of a value of type T.
 */
export interface StringConverter<T> {
  fromString(this: void, value: string): T | undefined;
  toString(this: void, value: T | undefined): string;
};
