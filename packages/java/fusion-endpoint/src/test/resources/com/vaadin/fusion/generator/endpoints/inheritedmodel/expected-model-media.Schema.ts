import ExternalDocumentation from '../ExternalDocumentation';
import Discriminator from './Discriminator';
import XML from './XML';

/**
 * This module is generated from io.swagger.v3.oas.models.media.Schema.
 * All changes to this file are overridden. Please consider to make changes in the corresponding Java file if necessary.
 */
export default interface Schema {
  readonly ref?: string;
  readonly _default?: any;
  readonly _enum?: ReadonlyArray<any | undefined>;
  readonly additionalProperties?: any;
  readonly deprecated?: boolean;
  readonly description?: string;
  readonly discriminator?: Discriminator;
  readonly example?: any;
  readonly exclusiveMaximum?: boolean;
  readonly exclusiveMinimum?: boolean;
  readonly extensions?: Readonly<Record<string, any | undefined>>;
  readonly externalDocs?: ExternalDocumentation;
  readonly format?: string;
  readonly maxItems?: number;
  readonly maxLength?: number;
  readonly maxProperties?: number;
  readonly maximum?: number;
  readonly minItems?: number;
  readonly minLength?: number;
  readonly minProperties?: number;
  readonly minimum?: number;
  readonly multipleOf?: number;
  readonly name?: string;
  readonly not?: Schema;
  readonly nullable?: boolean;
  readonly pattern?: string;
  readonly properties?: Readonly<Record<string, Schema | undefined>>;
  readonly readOnly?: boolean;
  readonly required?: ReadonlyArray<string | undefined>;
  readonly title?: string;
  readonly type?: string;
  readonly uniqueItems?: boolean;
  readonly writeOnly?: boolean;
  readonly xml?: XML;
}
