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
import type DependencyManager from '@vaadin/generator-typescript-utils/dependencies/DependencyManager';
import type { ArrayLiteralExpression, Identifier, TypeNode } from 'typescript';
import ts from 'typescript';

export default class ModelSchemaProcessor {
  readonly #schema: Schema;
  readonly #dependencies: DependencyManager;
  readonly #cwd: string;

  public constructor(schema: Schema, dependencies: DependencyManager, cwd: string) {
    this.#schema = schema;
    this.#dependencies = dependencies;
    this.#cwd = cwd;
  }

  public process(): [TypeNode, Identifier, ArrayLiteralExpression] {
    const unwrappedSchema = (
      isComposedSchema(this.#schema) ? decomposeSchema(this.#schema)[0] : this.#schema
    ) as NonComposedSchema;

    let model: Identifier;
    let type: TypeNode;
    let modelPath: string;
    let modelName: string;
    if (isReferenceSchema(unwrappedSchema)) {
      const schemaPath = convertReferenceSchemaToPath(unwrappedSchema);
      const typeName = convertReferenceSchemaToSpecifier(unwrappedSchema);
      const typePath = this.#dependencies.paths.createRelativePath(schemaPath, this.#cwd);
      modelPath = this.#dependencies.paths.createRelativePath(`${schemaPath}Model`, this.#cwd);
      modelName = `${typeName}Model`;
      const refType =
        this.#dependencies.imports.default.getIdentifier(typePath) ??
        this.#dependencies.imports.default.add(typePath, typeName, true);
      type = ts.factory.createTypeReferenceNode(refType);
      model =
        this.#dependencies.imports.default.getIdentifier(modelPath) ??
        this.#dependencies.imports.default.add(modelPath, modelName);
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
      model =
        this.#dependencies.imports.named.getIdentifier(modelPath, modelName) ??
        this.#dependencies.imports.named.add(modelPath, modelName);
    }

    const optionalArg = isNullableSchema(this.#schema) ? ts.factory.createTrue() : ts.factory.createFalse();

    return [type, model, ts.factory.createArrayLiteralExpression([optionalArg])];
  }
}
