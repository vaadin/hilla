/**
 * This module is generated from dev.hilla.generator.endpoints.nonnullable.NonNullableEndpoint.NonNullableModel.
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
 */
export default interface NonNullableModel {
  integers?: Array<number>;
  integersList?: Array<number | undefined>;
  foo: string;
  shouldBeNotNullByDefault: number;
  first: number;
  second: number;
  third: number;
  nullableInteger?: number;
  listOfMapNullable?: Array<Record<string, string>>;
  listOfMapNullableNotNull?: Array<Record<string, string | undefined> | undefined>;
}
