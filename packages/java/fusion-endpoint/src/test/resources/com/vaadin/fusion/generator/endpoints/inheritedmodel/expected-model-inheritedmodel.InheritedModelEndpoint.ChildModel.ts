import Version from '../../../../../../fasterxml/jackson/core/Version';
import ParentModel from './ParentModel';
import ArraySchema from '../../../../../../../io/swagger/v3/oas/models/media/ArraySchema';

/**
 * This module is generated from com.vaadin.fusion.generator.endpoints.inheritedmodel.InheritedModelEndpoint.ChildModel.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface ChildModel extends ParentModel {
  readonly abc?: ReadonlyArray<Readonly<Record<string, Version | undefined>> | undefined>;
  readonly def?: ReadonlyArray<Readonly<Record<string, Readonly<Record<string, Version | undefined>> | undefined>> | undefined>;
  readonly name?: string;
  readonly testObject?: ArraySchema;
}