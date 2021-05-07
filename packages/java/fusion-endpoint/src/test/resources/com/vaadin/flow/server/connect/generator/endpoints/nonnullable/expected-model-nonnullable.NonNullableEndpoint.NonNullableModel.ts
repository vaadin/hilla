/**
 * This module is generated from com.vaadin.flow.server.connect.generator.endpoints.nonnullable.NonNullableEndpoint.NonNullableModel.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface NonNullableModel {
  foo: string;
  listOfMapNullable?: Array<{ [key: string]: string; }>;
  listOfMapNullableNotNull?: Array<{ [key: string]: string; }>;
  nullableInteger?: number;
  shouldBeNotNullByDefault: number;
}