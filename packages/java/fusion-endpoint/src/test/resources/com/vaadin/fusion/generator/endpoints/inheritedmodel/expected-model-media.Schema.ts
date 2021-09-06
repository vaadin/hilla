import ExternalDocumentation from '../ExternalDocumentation';
import Discriminator from './Discriminator';
import XML from './XML';
/**
* This module is generated from io.swagger.v3.oas.models.media.Schema.
* All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
*/
export default interface Schema {
  readonly _default?: any;
  readonly name?: string;
  readonly title?: string;
  readonly multipleOf?: number;
  readonly maximum?: number;
  readonly exclusiveMaximum?: boolean;
  readonly minimum?: number;
  readonly exclusiveMinimum?: boolean;
  readonly maxLength?: number;
  readonly minLength?: number;
  readonly pattern?: string;
  readonly maxItems?: number;
  readonly minItems?: number;
  readonly uniqueItems?: boolean;
  readonly maxProperties?: number;
  readonly minProperties?: number;
  readonly required?: ReadonlyArray<string | undefined>;
  readonly type?: string;
  readonly not?: Schema;
  readonly properties?: Readonly<Record<string, Schema | undefined>>;
  readonly additionalProperties?: any;
  readonly description?: string;
  readonly format?: string;
  readonly ref?: string;
  readonly nullable?: boolean;
  readonly readOnly?: boolean;
  readonly writeOnly?: boolean;
  readonly example?: any;
  readonly externalDocs?: ExternalDocumentation;
  readonly deprecated?: boolean;
  readonly xml?: XML;
  readonly extensions?: Readonly<Record<string, any | undefined>>;
  readonly _enum?: ReadonlyArray<any | undefined>;
  readonly discriminator?: Discriminator;
  readonly exampleSetFlag: boolean;
}