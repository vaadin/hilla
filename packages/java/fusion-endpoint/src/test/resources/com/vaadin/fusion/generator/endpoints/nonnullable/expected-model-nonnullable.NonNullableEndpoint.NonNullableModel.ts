/**
 * This module is generated from com.vaadin.fusion.generator.endpoints.nonnullable.NonNullableEndpoint.NonNullableModel.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface NonNullableModel {
  readonly integers?: ReadonlyArray<number>;
  readonly integersList?: ReadonlyArray<number | undefined>;
  readonly foo: string;
  readonly shouldBeNotNullByDefault: number;
  readonly first: number;
  readonly second: number;
  readonly third: number;
  readonly nullableInteger?: number;
  readonly listOfMapNullable?: ReadonlyArray<Readonly<Record<string, string>>>;
  readonly listOfMapNullableNotNull?: ReadonlyArray<Readonly<Record<string, string | undefined>> | undefined>;
}