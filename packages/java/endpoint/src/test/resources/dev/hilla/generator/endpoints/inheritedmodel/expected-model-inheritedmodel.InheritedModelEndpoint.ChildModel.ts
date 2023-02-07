import Version from '../../../../../../com/fasterxml/jackson/core/Version';
import ParentModel from './ParentModel';
import ArraySchema from '../../../../../../io/swagger/v3/oas/models/media/ArraySchema';

/**
 * This module is generated from dev.hilla.generator.endpoints.inheritedmodel.InheritedModelEndpoint.ChildModel.
 * All changes to this file are overridden. Consider editing the corresponding Java file if necessary.
 */
export default interface ChildModel extends ParentModel {
  name?: string;
  testObject?: ArraySchema;
  abc?: Array<Record<string, Version | undefined> | undefined>;
  def?: Array<Record<string, Record<string, Version | undefined> | undefined> | undefined>;
}
