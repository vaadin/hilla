/* eslint-disable no-use-before-define */
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';

const COMPONENTS_SCHEMAS_REF = /#\/components\/schemas/;
const QUALIFIED_NAME_DELIMITER = /[$.]/g;

export default abstract class Schema {
  public static of(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>): Schema {
    const unwrappedSchema = 'anyOf' in schema ? schema.anyOf![0] : schema;
    const nullable = 'nullable' in schema ? schema.nullable : false;

    if ('$ref' in unwrappedSchema) {
      return new ReferenceSchema(unwrappedSchema, nullable);
    }

    const { additionalProperties, properties, type } = schema as ReadonlyDeep<OpenAPIV3.SchemaObject>;

    if (type === 'array') {
      return new ArraySchema(schema as ReadonlyDeep<OpenAPIV3.ArraySchemaObject>, nullable);
    }

    if (type === 'object') {
      if (!properties && !!additionalProperties) {
        return new MapSchema(schema as ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>, nullable);
      }

      return new ObjectSchema(schema as ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>, nullable);
    }

    return new RegularSchema(schema as ReadonlyDeep<OpenAPIV3.SchemaObject>, nullable);
  }

  readonly #nullable: boolean;

  protected constructor(nullable = false) {
    this.#nullable = nullable;
  }

  public abstract get internal(): ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>;

  public isArray(): this is ArraySchema {
    return false;
  }

  public isBoolean(): this is RegularSchema {
    return false;
  }

  public isInteger(): this is RegularSchema {
    return false;
  }

  public isMap(): this is MapSchema {
    return false;
  }

  public isNullable(): boolean {
    return this.#nullable;
  }

  public isNumber(): this is RegularSchema {
    return false;
  }

  public isObject(): this is ObjectSchema {
    return false;
  }

  public isReference(): this is ReferenceSchema {
    return false;
  }

  public isString(): this is RegularSchema {
    return false;
  }
}

export class ReferenceSchema extends Schema {
  readonly #schema: ReadonlyDeep<OpenAPIV3.ReferenceObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject>, nullable: boolean | undefined) {
    super(nullable);
    this.#schema = schema;
  }

  public get internal(): ReadonlyDeep<OpenAPIV3.ReferenceObject> {
    return this.#schema;
  }

  public get identifier(): string {
    const { $ref } = this.#schema;

    return $ref.substring($ref.lastIndexOf($ref.includes('$') ? '$' : '.') + 1, $ref.length);
  }

  public get path(): string {
    return `.${this.#schema.$ref.replace(COMPONENTS_SCHEMAS_REF, '').replace(QUALIFIED_NAME_DELIMITER, '/')}`;
  }

  public override isReference(): this is ReferenceSchema {
    return true;
  }
}

export class RegularSchema extends Schema {
  readonly #schema: ReadonlyDeep<OpenAPIV3.SchemaObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.SchemaObject>, nullable: boolean | undefined) {
    super(nullable);
    this.#schema = schema;
  }

  public get internal(): ReadonlyDeep<OpenAPIV3.SchemaObject> {
    return this.#schema;
  }

  public override isBoolean(): this is RegularSchema {
    return this.#schema.type === 'boolean';
  }

  public override isInteger(): this is RegularSchema {
    return this.#schema.type === 'integer';
  }

  public override isNumber(): this is RegularSchema {
    return this.#schema.type === 'number';
  }

  public override isString(): this is RegularSchema {
    return this.#schema.type === 'string';
  }
}

export class ArraySchema extends RegularSchema {
  readonly #schema: ReadonlyDeep<OpenAPIV3.ArraySchemaObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.ArraySchemaObject>, nullable: boolean | undefined) {
    super(schema, nullable);
    this.#schema = schema;
  }

  public override isArray(): this is ArraySchema {
    return true;
  }

  public get items(): Schema {
    return Schema.of((this.#schema as ReadonlyDeep<OpenAPIV3.ArraySchemaObject>).items);
  }
}

export class ObjectSchema extends RegularSchema {
  readonly #schema: ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>, nullable: boolean | undefined) {
    super(schema, nullable);
    this.#schema = schema;
  }

  public get properties(): IterableIterator<[name: string, schema: Schema]> | undefined {
    const { properties } = this.#schema;

    if (!properties) {
      return undefined;
    }

    const list = Object.entries(properties).map(([name, schema]) => [name, Schema.of(schema)]);

    return list[Symbol.iterator]() as IterableIterator<[name: string, schema: Schema]>;
  }

  public override isObject(): this is ObjectSchema {
    return true;
  }

  public isEmptyObject(): boolean {
    return !this.#schema.properties;
  }
}

export class MapSchema extends ObjectSchema {
  readonly #schema: ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>, nullable: boolean | undefined) {
    super(schema, nullable);
    this.#schema = schema;
  }

  public get values(): Schema | undefined {
    const { additionalProperties } = this.#schema;

    return typeof additionalProperties === 'boolean' ? undefined : Schema.of(additionalProperties!);
  }

  public override isMap(): this is MapSchema {
    return true;
  }
}
