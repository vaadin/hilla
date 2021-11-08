/* eslint-disable no-use-before-define */
import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import { convertFullyQualifiedNameToRelativePath, simplifyFullyQualifiedName } from './utils.js';

const COMPONENTS_SCHEMAS_REF = /#\/components\/schemas\//;

export default abstract class Schema {
  public static of(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>): Schema {
    const _schema = 'anyOf' in schema ? schema.anyOf![0] : schema;
    const nullable = 'nullable' in schema ? schema.nullable : false;

    if ('$ref' in _schema) {
      return new ReferenceSchema(_schema, nullable);
    }

    const { additionalProperties, properties, type } = _schema as ReadonlyDeep<OpenAPIV3.SchemaObject>;

    if (type === 'array') {
      return new ArraySchema(_schema as ReadonlyDeep<OpenAPIV3.ArraySchemaObject>, nullable);
    }

    if (type === 'object') {
      if (!properties && !!additionalProperties) {
        return new MapSchema(_schema as ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>, nullable);
      }

      return new ObjectSchema(_schema as ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>, nullable);
    }

    if (type === 'string' && 'enum' in _schema) {
      return new EnumSchema(_schema as ReadonlyDeep<OpenAPIV3.SchemaObject>, nullable);
    }

    return new RegularSchema(_schema as ReadonlyDeep<OpenAPIV3.SchemaObject>, nullable);
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

  public isEnum(): this is EnumSchema {
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

  public get specifier(): string {
    return simplifyFullyQualifiedName(this.#schema.$ref);
  }

  public get internal(): ReadonlyDeep<OpenAPIV3.ReferenceObject> {
    return this.#schema;
  }

  public get path(): string {
    return convertFullyQualifiedNameToRelativePath(this.#schema.$ref.replace(COMPONENTS_SCHEMAS_REF, ''));
  }

  public override isReference(): this is ReferenceSchema {
    return true;
  }
}

const $schema = Symbol('schema');

export class RegularSchema extends Schema {
  protected readonly [$schema]: ReadonlyDeep<OpenAPIV3.SchemaObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.SchemaObject>, nullable: boolean | undefined) {
    super(nullable);
    this[$schema] = schema;
  }

  public get internal(): ReadonlyDeep<OpenAPIV3.SchemaObject> {
    return this[$schema];
  }

  public override isBoolean(): this is RegularSchema {
    return this[$schema].type === 'boolean';
  }

  public override isInteger(): this is RegularSchema {
    return this[$schema].type === 'integer';
  }

  public override isNumber(): this is RegularSchema {
    return this[$schema].type === 'number';
  }

  public override isString(): this is RegularSchema {
    return this[$schema].type === 'string';
  }
}

export class ArraySchema extends RegularSchema {
  public get items(): Schema {
    return Schema.of((this[$schema] as ReadonlyDeep<OpenAPIV3.ArraySchemaObject>).items);
  }

  public override isArray(): this is ArraySchema {
    return true;
  }
}

export class ObjectSchema extends RegularSchema {
  public get properties(): IterableIterator<[name: string, schema: Schema]> | undefined {
    const { properties } = this[$schema];

    if (!properties) {
      return undefined;
    }

    const list = Object.entries(properties).map(([name, schema]) => [name, Schema.of(schema)]);

    return list[Symbol.iterator]() as IterableIterator<[name: string, schema: Schema]>;
  }

  public isEmptyObject(): boolean {
    return !this[$schema].properties;
  }

  public override isObject(): this is ObjectSchema {
    return true;
  }
}

export class MapSchema extends ObjectSchema {
  public get valuesType(): Schema | undefined {
    const { additionalProperties } = this[$schema];

    return typeof additionalProperties === 'boolean' ? undefined : Schema.of(additionalProperties!);
  }

  public override isMap(): this is MapSchema {
    return true;
  }
}

export class EnumSchema extends RegularSchema {
  public get members(): readonly string[] | undefined {
    return this[$schema].enum;
  }

  public override isEnum(): this is EnumSchema {
    return true;
  }
}
