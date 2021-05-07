import Version from '../../../../../../../../fasterxml/jackson/core/Version';
import ParentModel from './ParentModel';
import ArraySchema from '../../../../../../../../../io/swagger/v3/oas/models/media/ArraySchema';

/**
 * This module is generated from com.vaadin.flow.server.connect.generator.endpoints.inheritedmodel.InheritedModelEndpoint.ChildModel.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface ChildModel extends ParentModel {
  abc?: Array<{ [key: string]: Version; }>;
  def?: Array<{ [key: string]: { [key: string]: Version; }; }>;
  name?: string;
  testObject?: ArraySchema;
}