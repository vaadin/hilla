/**
 * This module is generated from com.vaadin.fusion.generator.endpoints.nonnullable.NonNullableEndpoint.NonNullableModel.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface NonNullableModel {
  first: number;
  foo: string;
  integers?: Array<number>;
  integersList?: Array<number | undefined>;
  listOfMapNullable?: Array<Record<string, string>>;
  listOfMapNullableNotNull?: Array<Record<string, string | undefined> | undefined>;
  nullableInteger?: number;
  second: number;
  shouldBeNotNullByDefault: number;
  third: number;
}