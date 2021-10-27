import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { TypeNode } from 'typescript';
import { factory, SyntaxKind } from 'typescript';
import Schema, { ReferenceSchema } from '../../core/Schema';
import type { BackbonePluginContext } from './utils';

export default class SchemaProcessor {
  readonly #context: BackbonePluginContext;
  readonly #schema: Schema;

  public constructor(schema: Schema, context: BackbonePluginContext) {
    this.#context = context;
    this.#schema = schema;
  }

  public process(): TypeNode {
    let node: TypeNode;

    if (this.#schema.isReference) {
      node = this.#processReference();
    } else if (this.#schema.isArray) {
      node = this.#processArray();
    } else if (this.#schema.isMap) {
      node = this.#processMap();
    } else if (this.#schema.isBoolean) {
      node = this.#processBoolean();
    } else if (this.#schema.isInteger || this.#schema.isNumber) {
      node = this.#processNumber();
    } else if (this.#schema.isString) {
      node = this.#processString();
    } else {
      node = this.#processUnknown();
    }

    return this.#schema.isNullable ? this.#wrapNullable(node) : node;
  }

  #processReference(): TypeNode {
    const { identifier, path } = this.#schema as ReferenceSchema;

    this.#context.imports.set(identifier, path);

    return factory.createTypeReferenceNode(identifier);
  }

  #processArray(): TypeNode {
    const { items } = this.#schema.inner as ReadonlyDeep<OpenAPIV3.ArraySchemaObject>;

    const itemsProcessor = new SchemaProcessor(Schema.of(items), this.#context);

    return factory.createTypeReferenceNode('Array', [itemsProcessor.process()]);
  }

  #processMap(): TypeNode {
    const { additionalProperties } = this.#schema.inner as ReadonlyDeep<OpenAPIV3.NonArraySchemaObject>;

    const additionalPropertiesProcessor = new SchemaProcessor(
      Schema.of(additionalProperties as ReadonlyDeep<OpenAPIV3.SchemaObject>),
      this.#context
    );

    return factory.createTypeReferenceNode('Record', [
      factory.createKeywordTypeNode(SyntaxKind.StringKeyword),
      additionalPropertiesProcessor.process(),
    ]);
  }

  #processBoolean(): TypeNode {
    return factory.createKeywordTypeNode(SyntaxKind.BooleanKeyword);
  }

  #processNumber(): TypeNode {
    return factory.createKeywordTypeNode(SyntaxKind.NumberKeyword);
  }

  #processString(): TypeNode {
    return factory.createKeywordTypeNode(SyntaxKind.StringKeyword);
  }

  #processUnknown(): TypeNode {
    return factory.createKeywordTypeNode(SyntaxKind.UnknownKeyword);
  }

  #wrapNullable(node: TypeNode): TypeNode {
    return factory.createUnionTypeNode([node, factory.createKeywordTypeNode(SyntaxKind.UndefinedKeyword)]);
  }
}
