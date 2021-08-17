import Role from './Role';

/**
 * This module is generated from com.vaadin.fusion.generator.endpoints.json.JsonTestEndpoint.User.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */

export default interface User {
  readonly name?: string;
  readonly password?: string;
  readonly roles?: Readonly<Record<string, Role | undefined>>;
  readonly optionalField?: string;
}