import {
  isNullableSchema,
  Schema,
  decomposeSchema,
  isComposedSchema,
  NonComposedSchema,
  isReferenceSchema,
  isNumberSchema,
  isIntegerSchema,
  isStringSchema,
  isMapSchema,
  isArraySchema,
  isBooleanSchema,
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
} from '@vaadin/generator-typescript-core/Schema.js';
import type { ArrayLiteralExpression, Identifier, TypeNode } from 'typescript';
import ts from 'typescript';
import type DependencyManager from './DependencyManager';

export default class ModelSchemaProcessor {
  #schema: Schema;
  #dependencies: DependencyManager;

  public constructor(schema: Schema, dependencies: DependencyManager) {
    this.#schema = schema;
    this.#dependencies = dependencies;
  }

  public process(): [Identifier, TypeNode, ArrayLiteralExpression] {
    const unwrappedSchema = (
      isComposedSchema(this.#schema) ? decomposeSchema(this.#schema)[0] : this.#schema
    ) as NonComposedSchema;

    let type: TypeNode;
    let modelPath: string;
    let modelName: string;
    if (isReferenceSchema(unwrappedSchema)) {
      const typePath = convertReferenceSchemaToPath(unwrappedSchema);
      const typeName = convertReferenceSchemaToSpecifier(unwrappedSchema);
      modelPath = `${typePath}Model`;
      modelName = `${typeName}Model`;
      const refType =
        this.#dependencies.imports.getIdentifier(typeName, typePath) ??
        this.#dependencies.imports.register(typeName, typePath);
      type = ts.factory.createTypeReferenceNode(refType);
    } else {
      modelPath = '@vaadin/form';
      if (isArraySchema(unwrappedSchema)) {
        type = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('ReadonlyArray'));
        modelName = 'ArrayModel';
      } else if (isMapSchema(unwrappedSchema)) {
        type = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('Record'));
        modelName = 'ObjectModel';
      } else if (isStringSchema(unwrappedSchema)) {
        type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword);
        modelName = 'StringModel';
      } else if (isNumberSchema(unwrappedSchema)) {
        type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
        modelName = 'NumberModel';
      } else if (isIntegerSchema(unwrappedSchema)) {
        type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
        modelName = 'NumberModel';
      } else if (isBooleanSchema(unwrappedSchema)) {
        type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.BooleanKeyword);
        modelName = 'BooleanModel';
      } else {
        type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
        modelName = 'ObjectModel';
      }
    }

    const model =
      this.#dependencies.imports.getIdentifier(modelName, modelPath) ??
      this.#dependencies.imports.register(modelName, modelPath);

    const optionalArg = isNullableSchema(this.#schema) ? ts.factory.createTrue() : ts.factory.createFalse();

    return [model, type, ts.factory.createArrayLiteralExpression([optionalArg])];
  }

  #getOrRegisterImport(specifier: string, path: string) {
    return (
      this.#dependencies.imports.getIdentifier(specifier, path) ?? this.#dependencies.imports.register(specifier, path)
    );
  }
}
