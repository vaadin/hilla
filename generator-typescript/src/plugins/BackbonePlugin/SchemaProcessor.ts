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
  unwrapSchema,
} from '../../core/Schema.js';
import { createSourceBag } from './SourceBag.js';
import type { SourceBag, SourceBagBase, TypeNodesBag } from './SourceBag.js';

type SingleTypeNodeBag = SourceBagBase & Readonly<{ node: TypeNode }>;

function createSingleTypeNodeBag(
  node: TypeNode,
  imports: Readonly<Record<string, string>> = {},
  exports: Readonly<Record<string, string>> = {},
): SingleTypeNodeBag {
  return {
    node,
    imports,
    exports,
  };
}

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

  readonly #schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>;

  public constructor(schema: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>) {
    this.#schema = schema;
  }

  public ['constructor']: typeof SchemaProcessor;

  public process(): TypeNodesBag {
    let imports: SourceBag['imports'] | undefined;
    let exports: SourceBag['exports'] | undefined;
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

    return createSourceBag(
      isNullableSchema(this.#schema) ? [node, this.constructor.#createUndefined()] : [node],
      imports,
      exports,
    );
  }

  #processReference(): SingleTypeNodeBag {
    const { identifier, path } = getReferenceSchemaDetails(this.#schema as ReadonlyDeep<OpenAPIV3.ReferenceObject>);

    return createSingleTypeNodeBag(ts.factory.createTypeReferenceNode(identifier), { [identifier]: path });
  }

  #processArray(): SingleTypeNodeBag {
    const { items } = unwrapSchema(this.#schema) as ReadonlyDeep<OpenAPIV3.ArraySchemaObject>;

    const { imports, code } = new SchemaProcessor(items).process();

    return createSingleTypeNodeBag(
      ts.factory.createTypeReferenceNode('Array', [ts.factory.createUnionTypeNode(code)]),
      imports,
    );
  }

  #processMap(): SingleTypeNodeBag {
    const { additionalProperties } = unwrapSchema(this.#schema) as ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>;

    const { imports, code } = new SchemaProcessor(
      additionalProperties as ReadonlyDeep<OpenAPIV3.SchemaObject>,
    ).process();

    return createSingleTypeNodeBag(
      ts.factory.createTypeReferenceNode('Record', [
        ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
        ts.factory.createUnionTypeNode(code),
      ]),
      imports,
    );
  }
}
