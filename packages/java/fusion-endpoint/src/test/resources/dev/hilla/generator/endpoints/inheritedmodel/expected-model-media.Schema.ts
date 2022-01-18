import ExternalDocumentation from '../ExternalDocumentation';
import Discriminator from './Discriminator';
import XML from './XML';
/**
* This module is generated from io.swagger.v3.oas.models.media.Schema.
* All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
*/
export default interface Schema {
  _default?: any;
  name?: string;
  title?: string;
  multipleOf?: number;
  maximum?: number;
  exclusiveMaximum?: boolean;
  minimum?: number;
  exclusiveMinimum?: boolean;
  maxLength?: number;
  minLength?: number;
  pattern?: string;
  maxItems?: number;
  minItems?: number;
  uniqueItems?: boolean;
  maxProperties?: number;
  minProperties?: number;
  required?: Array<string | undefined>;
  type?: string;
  not?: Schema;
  properties?: Record<string, Schema | undefined>;
  additionalProperties?: any;
  description?: string;
  format?: string;
  ref?: string;
  nullable?: boolean;
  readOnly?: boolean;
  writeOnly?: boolean;
  example?: any;
  externalDocs?: ExternalDocumentation;
  deprecated?: boolean;
  xml?: XML;
  extensions?: Record<string, any | undefined>;
  _enum?: Array<any | undefined>;
  discriminator?: Discriminator;
}
