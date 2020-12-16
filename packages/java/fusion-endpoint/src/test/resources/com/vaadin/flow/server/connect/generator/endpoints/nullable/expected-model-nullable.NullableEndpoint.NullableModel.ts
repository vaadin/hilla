/**
 * This module is generated from com.vaadin.flow.server.connect.generator.endpoints.nullable.NullableEndpoint.NullableModel.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface NullableModel {
  bar: string;
  foo: string;
  listOfMapNullable?: Array<{ [key: string]: string; }>;
  listOfMapNullableNotNull?: Array<{ [key: string]: string; }>;
  nullableInteger?: number;
  shouldBeNotNullByDefault: number;
}