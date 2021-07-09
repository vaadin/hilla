/**
 * This module is generated from com.vaadin.fusion.generator.endpoints.nonnullable.NonNullableEndpoint.NonNullableModel.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface NonNullableModel {
  readonly first: number;
  readonly foo: string;
  readonly integers?: ReadonlyArray<number>;
  readonly integersList?: ReadonlyArray<number | undefined>;
  readonly listOfMapNullable?: ReadonlyArray<Readonly<Record<string, string>>>;
  readonly listOfMapNullableNotNull?: ReadonlyArray<Readonly<Record<string, string | undefined>> | undefined>;
  readonly nullableInteger?: number;
  readonly second: number;
  readonly shouldBeNotNullByDefault: number;
  readonly third: number;
}