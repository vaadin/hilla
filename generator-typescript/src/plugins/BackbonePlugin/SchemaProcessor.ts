import type { TypeNode } from 'typescript';
import ts from 'typescript';
import type { ArraySchema, MapSchema, ReferenceSchema } from '../../core/Schema.js';
import type Schema from '../../core/Schema.js';
import type DependencyManager from './DependencyManager.js';

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

  static #createUndefined(): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.UndefinedKeyword);
  }

  static #createUnknown(): TypeNode {
    return ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
  }

  public ['constructor']: typeof SchemaProcessor;
  readonly #dependencies: DependencyManager;
  readonly #schema: Schema;

  public constructor(schema: Schema, dependencies: DependencyManager) {
    this.#schema = schema;
    this.#dependencies = dependencies;
  }

  public process(): readonly TypeNode[] {
    let node: TypeNode;

    if (this.#schema.isReference()) {
      node = this.#processReference();
    } else if (this.#schema.isArray()) {
      node = this.#processArray();
    } else if (this.#schema.isMap()) {
      node = this.#processMap();
    } else if (this.#schema.isBoolean()) {
      node = this.constructor.#createBoolean();
    } else if (this.#schema.isInteger() || this.#schema.isNumber()) {
      node = this.constructor.#createNumber();
    } else if (this.#schema.isString()) {
      node = this.constructor.#createString();
    } else {
      node = this.constructor.#createUnknown();
    }

    return this.#schema.isNullable() ? [node, this.constructor.#createUndefined()] : [node];
  }

  #processArray(): TypeNode {
    const nodes = new SchemaProcessor((this.#schema as ArraySchema).items, this.#dependencies).process();

    return ts.factory.createTypeReferenceNode('Array', [ts.factory.createUnionTypeNode(nodes)]);
  }

  #processMap(): TypeNode {
    const { valuesType } = this.#schema as MapSchema;

    let valuesTypeNode: TypeNode;

    if (valuesType) {
      const nodes = new SchemaProcessor(valuesType, this.#dependencies).process();
      valuesTypeNode = ts.factory.createUnionTypeNode(nodes);
    } else {
      valuesTypeNode = ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
    }

    return ts.factory.createTypeReferenceNode('Record', [
      ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
      valuesTypeNode,
    ]);
  }

  #processReference(): TypeNode {
    const { specifier, path } = this.#schema as ReferenceSchema;
    const identifier = this.#dependencies.imports.register(specifier, path);

    return ts.factory.createTypeReferenceNode(identifier);
  }
}
