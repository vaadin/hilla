import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { TypeNode } from 'typescript';
import ts from 'typescript';
import {
  getReferenceSchemaDetails,
  isArraySchema,
  isBooleanSchema,
  isIntegerSchema,
  isMapSchema,
  isNullableSchema,
  isNumberSchema,
  isReferenceSchema,
  isStringSchema,
} from '../../core/Schema.js';
import type { SourceBag, SourceBagBase, TypeNodesBag } from './utils.js';

export type Schema = ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>;

type SingleTypeNodeBag = SourceBagBase & Readonly<{ node: TypeNode }>;

export default class SchemaProcessor {
  static #createBoolean(): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.BooleanKeyword);
  }

  static #createNumber(): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
  }

  static #createString(): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword);
  }

  static #createUnknown(): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
  }

  static #createUndefined(): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.UndefinedKeyword);
  }

  readonly #schema: Schema;

  public constructor(schema: Schema) {
    this.#schema = schema;
  }

  public ['constructor']: typeof SchemaProcessor;

  public process(): TypeNodesBag {
    let imports: SourceBag['imports'];
    let exports: SourceBag['exports'];
    let node: TypeNode;

    if (isReferenceSchema(this.#schema)) {
      ({ node, exports, imports } = this.#processReference());
    } else if (isArraySchema(this.#schema)) {
      ({ node, exports, imports } = this.#processArray());
    } else if (isMapSchema(this.#schema)) {
      ({ node, exports, imports } = this.#processMap());
    } else if (isBooleanSchema(this.#schema)) {
      node = this.constructor.#createBoolean();
    } else if (isIntegerSchema(this.#schema) || isNumberSchema(this.#schema)) {
      node = this.constructor.#createNumber();
    } else if (isStringSchema(this.#schema)) {
      node = this.constructor.#createString();
    } else {
      node = this.constructor.#createUnknown();
    }

    return {
      code: isNullableSchema(this.#schema) ? [node, this.constructor.#createUndefined()] : [node],
      exports,
      imports,
    };
  }

  #processReference(): SingleTypeNodeBag {
    const { identifier, path } = getReferenceSchemaDetails(this.#schema as ReadonlyDeep<OpenAPIV3.ReferenceObject>);

    return {
      imports: { [identifier]: path },
      node: ts.factory.createTypeReferenceNode(identifier),
    };
  }

  #processArray(): SingleTypeNodeBag {
    const { items } = this.#schema as ReadonlyDeep<OpenAPIV3.ArraySchemaObject>;

    const { imports, code } = new SchemaProcessor(items).process();

    return {
      imports,
      node: ts.factory.createTypeReferenceNode('Array', [ts.factory.createUnionTypeNode(code)]),
    };
  }

  #processMap(): SingleTypeNodeBag {
    const { additionalProperties } = this.#schema as ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>;

    const { imports, code } = new SchemaProcessor(
      additionalProperties as ReadonlyDeep<OpenAPIV3.SchemaObject>,
    ).process();

    return {
      imports,
      node: ts.factory.createTypeReferenceNode('Record', [
        ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
        ts.factory.createUnionTypeNode(code),
      ]),
    };
  }
}
