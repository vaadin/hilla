import Permissions from './Permissions';

/**
 * Role bean
 * This module is generated from com.vaadin.fusion.generator.endpoints.json.JsonTestEndpoint.Role.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */

export default interface Role {
  /**
   * Description for permissions.
   */
  readonly permissions?: Permissions;
  /**
   * Description for roleName.
   */
  readonly roleName?: string;
}