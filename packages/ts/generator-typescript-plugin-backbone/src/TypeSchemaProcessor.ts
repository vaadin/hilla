import {
  ArraySchema,
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isArraySchema,
  isBooleanSchema,
  isComposedSchema,
  isIntegerSchema,
  isMapSchema,
  isNullableSchema,
  isNumberSchema,
  isReferenceSchema,
  isStringSchema,
  MapSchema,
  NonComposedSchema,
  ReferenceSchema,
  Schema,
} from '@vaadin/generator-typescript-core/Schema.js';
import type DependencyManager from '@vaadin/generator-typescript-utils/dependencies/DependencyManager.js';
import type { TypeNode } from 'typescript';
import ts from 'typescript';

function createBoolean(): TypeNode {
  return ts.factory.createKeywordTypeNode(ts.SyntaxKind.BooleanKeyword);
}

function createNumber(): TypeNode {
  return ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
}

function createString(): TypeNode {
  return ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword);
}

function createUndefined(): TypeNode {
  return ts.factory.createKeywordTypeNode(ts.SyntaxKind.UndefinedKeyword);
}

function createUnknown(): TypeNode {
  return ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
}

function unwrapPossiblyNullableSchema(schema: Schema): NonComposedSchema {
  if (isComposedSchema(schema)) {
    const [result] = decomposeSchema(schema);

    return result as NonComposedSchema;
  }

  return schema as NonComposedSchema;
}

export default class TypeSchemaProcessor {
  public declare ['constructor']: typeof TypeSchemaProcessor;
  readonly #dependencies: DependencyManager;
  readonly #schema: Schema;

  public constructor(schema: Schema, dependencies: DependencyManager) {
    this.#schema = schema;
    this.#dependencies = dependencies;
  }

  public process(): readonly TypeNode[] {
    let node: TypeNode;

    const unwrappedSchema = unwrapPossiblyNullableSchema(this.#schema);

    if (isReferenceSchema(unwrappedSchema)) {
      node = this.#processReference(unwrappedSchema);
    } else if (isArraySchema(unwrappedSchema)) {
      node = this.#processArray(unwrappedSchema);
    } else if (isMapSchema(unwrappedSchema)) {
      node = this.#processMap(unwrappedSchema);
    } else if (isBooleanSchema(unwrappedSchema)) {
      node = createBoolean();
    } else if (isIntegerSchema(unwrappedSchema) || isNumberSchema(unwrappedSchema)) {
      node = createNumber();
    } else if (isStringSchema(unwrappedSchema)) {
      node = createString();
    } else {
      node = createUnknown();
    }

    return isNullableSchema(this.#schema) ? [node, createUndefined()] : [node];
  }

  #processArray(schema: ArraySchema): TypeNode {
    const nodes = new TypeSchemaProcessor(schema.items, this.#dependencies).process();

    return ts.factory.createTypeReferenceNode('Array', [ts.factory.createUnionTypeNode(nodes)]);
  }

  #processMap({ additionalProperties: valuesType }: MapSchema): TypeNode {
    let valuesTypeNode: TypeNode;

    if (typeof valuesType !== 'boolean') {
      const nodes = new TypeSchemaProcessor(valuesType, this.#dependencies).process();
      valuesTypeNode = ts.factory.createUnionTypeNode(nodes);
    } else {
      valuesTypeNode = ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
    }

    return ts.factory.createTypeReferenceNode('Record', [
      ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
      valuesTypeNode,
    ]);
  }

  #processReference(schema: ReferenceSchema): TypeNode {
    const { imports, paths } = this.#dependencies;

    const specifier = convertReferenceSchemaToSpecifier(schema);
    const path = paths.createRelativePath(convertReferenceSchemaToPath(schema));

    const identifier = imports.default.getIdentifier(path) ?? imports.default.add(path, specifier, true);

    return ts.factory.createTypeReferenceNode(identifier);
  }
}
