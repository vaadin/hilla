import type { TypeNode } from 'typescript';
import ts from 'typescript';
import type { ArraySchema, MapSchema, ReferenceSchema } from '../../core/Schema.js';
import type Schema from '../../core/Schema.js';
import { createSourceBag, ExportList, ImportList } from './SourceBag.js';
import type { SourceBag, SourceBagBase, TypeNodesBag } from './SourceBag.js';

type SingleTypeNodeBag = SourceBagBase & Readonly<{ node: TypeNode }>;

function createSingleTypeNodeBag(
  node: TypeNode,
  imports: ImportList = {},
  exports: ExportList = {},
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

  readonly #schema: Schema;

  public constructor(schema: Schema) {
    this.#schema = schema;
  }

  public ['constructor']: typeof SchemaProcessor;

  public process(): TypeNodesBag {
    let imports: SourceBag['imports'] | undefined;
    let exports: SourceBag['exports'] | undefined;
    let node: TypeNode;

    if (this.#schema.isReference()) {
      ({ node, exports, imports } = this.#processReference());
    } else if (this.#schema.isArray()) {
      ({ node, exports, imports } = this.#processArray());
    } else if (this.#schema.isMap()) {
      ({ node, exports, imports } = this.#processMap());
    } else if (this.#schema.isBoolean()) {
      node = this.constructor.#createBoolean();
    } else if (this.#schema.isInteger() || this.#schema.isNumber()) {
      node = this.constructor.#createNumber();
    } else if (this.#schema.isString()) {
      node = this.constructor.#createString();
    } else {
      node = this.constructor.#createUnknown();
    }

    return createSourceBag(
      this.#schema.isNullable() ? [node, this.constructor.#createUndefined()] : [node],
      imports,
      exports,
    );
  }

  #processReference(): SingleTypeNodeBag {
    const { identifier, path } = this.#schema as ReferenceSchema;

    return createSingleTypeNodeBag(ts.factory.createTypeReferenceNode(identifier), { [identifier]: path });
  }

  #processArray(): SingleTypeNodeBag {
    const { imports, code } = new SchemaProcessor((this.#schema as ArraySchema).items).process();

    return createSingleTypeNodeBag(
      ts.factory.createTypeReferenceNode('Array', [ts.factory.createUnionTypeNode(code)]),
      imports,
    );
  }

  #processMap(): SingleTypeNodeBag {
    const { values } = this.#schema as MapSchema;

    let imports: ImportList | undefined;
    let valuesTypeNode: TypeNode;

    if (values) {
      const { imports: _imports, code } = new SchemaProcessor(values).process();
      imports = _imports;
      valuesTypeNode = ts.factory.createUnionTypeNode(code);
    } else {
      valuesTypeNode = ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
    }

    return createSingleTypeNodeBag(
      ts.factory.createTypeReferenceNode('Record', [
        ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
        valuesTypeNode,
      ]),
      imports,
    );
  }
}
