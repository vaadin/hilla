import Group from './Group';
import ModelFromDifferentPackage from '../subpackage/ModelFromDifferentPackage';

/**
 * This module is generated from com.vaadin.fusion.generator.endpoints.model.ModelEndpoint.Account.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface Account {
  readonly children?: Account;
  /**
   * Multiple line description should work.This is very very very very
   * very very very very long.
   */
  readonly groups?: ReadonlyArray<Group | undefined>;
  readonly modelFromDifferentPackage?: ModelFromDifferentPackage;
  /**
   * Javadoc for username.
   */
  readonly username?: string;
}