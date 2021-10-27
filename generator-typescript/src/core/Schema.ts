/* eslint-disable no-use-before-define */
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import NotImplementedException from './NotImplementedException';

export default abstract class Schema {
  public static of(
    schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
    nullable?: boolean
  ): Schema {
    if ('$ref' in schema) {
      return new ReferenceSchema(schema, nullable ?? false);
    }

    const { anyOf, type } = schema;

    return type === 'object' && anyOf?.length === 1
      ? Schema.of(anyOf[0], schema.nullable)
      : new RegularSchema(schema, nullable ?? schema.nullable ?? false);
  }

  readonly #nullable: boolean;

  protected constructor(nullable: boolean) {
    this.#nullable = nullable;
  }

  public get isArray(): boolean {
    return false;
  }

  public get isBoolean(): boolean {
    return false;
  }

  public get isInteger(): boolean {
    return false;
  }

  public get isMap(): boolean {
    return false;
  }

  public get isNullable(): boolean {
    return this.#nullable;
  }

  public get isNumber(): boolean {
    return false;
  }

  public get isObject(): boolean {
    return false;
  }

  public get isReference(): boolean {
    return false;
  }

  public get isString(): boolean {
    return false;
  }

  public abstract get inner(): ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>;
}

const COMPONENTS_SCHEMAS_REF = /#\/components\/schemas/;
const QUALIFIED_NAME_DELIMITER = /[$.]/g;

export class ReferenceSchema extends Schema {
  readonly #schema: ReadonlyDeep<OpenAPIV3.ReferenceObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject>, nullable: boolean) {
    super(nullable);
    this.#schema = schema;
  }

  public override get isObject(): boolean {
    return true;
  }

  public override get isReference(): boolean {
    return true;
  }

  public get identifier(): string {
    const { $ref } = this.#schema;

    return $ref.substring($ref.lastIndexOf('.'), $ref.length);
  }

  public get path(): string {
    return `/${this.#schema.$ref.replace(COMPONENTS_SCHEMAS_REF, '').replace(QUALIFIED_NAME_DELIMITER, '/')}`;
  }

  public override get inner(): ReadonlyDeep<OpenAPIV3.ReferenceObject> {
    return this.#schema;
  }
}

export class RegularSchema extends Schema {
  readonly #schema: ReadonlyDeep<OpenAPIV3.SchemaObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.SchemaObject>, nullable: boolean) {
    super(nullable);
    this.#schema = schema;
  }

  public override get isArray(): boolean {
    return this.#schema.type === 'array';
  }

  public override get isBoolean(): boolean {
    return this.#schema.type === 'boolean';
  }

  public override get isInteger(): boolean {
    return this.#schema.type === 'integer';
  }

  public override get isMap(): boolean {
    return this.isObject && !this.#schema.properties && !!this.#schema.additionalProperties;
  }

  public override get isNumber(): boolean {
    return this.#schema.type === 'number';
  }

  public override get isObject(): boolean {
    return this.#schema.type === 'object';
  }

  public override get isString(): boolean {
    return this.#schema.type === 'string';
  }

  public override get inner(): ReadonlyDeep<OpenAPIV3.SchemaObject> {
    return this.#schema;
  }
}
