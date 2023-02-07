import Group from './Group';
import ModelFromDifferentPackage from '../subpackage/ModelFromDifferentPackage';
/**
* This module is generated from dev.hilla.generator.endpoints.model.ModelEndpoint.Account.
* All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
*/
export default interface Account {
  /**
  * Javadoc for username.
  */
  username?: string;
  children?: Account;
  /**
  * Multiple line description should work.This is very very very very
  * very very very very long.
  */
  groups?: Array<Group | undefined>;
  modelFromDifferentPackage?: ModelFromDifferentPackage;
}
