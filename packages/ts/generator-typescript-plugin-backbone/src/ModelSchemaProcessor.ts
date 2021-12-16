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
  ReferenceSchema,
  ArraySchema,
  MapSchema,
  NonComposedRegularSchema,
  isNonComposedRegularSchema,
} from '@vaadin/generator-typescript-core/Schema.js';
import PluginError from '@vaadin/generator-typescript-utils/PluginError.js';
import type DependencyManager from '@vaadin/generator-typescript-utils/dependencies/DependencyManager';
import type { ArrayLiteralExpression, Expression, Identifier, TypeNode } from 'typescript';
import ts from 'typescript';

type AnnotatedSchema = NonComposedRegularSchema & Readonly<{ 'x-annotations': ReadonlyArray<string> }>;

function isAnnotatedSchema(schema: Schema): schema is AnnotatedSchema {
  return isNonComposedRegularSchema(schema) && 'x-annotations' in schema;
}

type AnnotationPrimitiveArgument = boolean | number | string;
type AnnotationArgument = AnnotationPrimitiveArgument | AnnotationNamedArguments;
interface AnnotationNamedArguments {
  readonly [name: string]: AnnotationArgument;
}

interface Annotation {
  simpleName: string;
  arguments: ReadonlyArray<AnnotationArgument>;
}

function parseAnnotation(annotationText: string): Annotation {
  const [, simpleName, argumentsText] = /^(\w+)\((.*)\)$/.exec(annotationText) || [];
  if (simpleName === undefined) {
    throw new PluginError(`Unknown annotation format when processing "${annotationText}"`);
  }
  let args: ReadonlyArray<AnnotationArgument> = [];
  if (argumentsText !== undefined) {
    const argumentsTextWithQuotedKeys = argumentsText.replace(/(\w+)\s*:/g, '"$1":');
    args = JSON.parse(`[${argumentsTextWithQuotedKeys}]`);
  }
  return { simpleName, arguments: args };
}

function convertAnnotationArgumentToExpression(arg: AnnotationArgument): Expression {
  switch (typeof arg) {
    case 'boolean':
      return arg ? ts.factory.createTrue() : ts.factory.createFalse();
      break;
    case 'number':
      return ts.factory.createNumericLiteral(arg);
      break;
    case 'string':
      return ts.factory.createStringLiteral(arg);
      break;
    case 'object':
      return ts.factory.createObjectLiteralExpression(
        Object.entries(arg).map(([key, value]) =>
          ts.factory.createPropertyAssignment(key, convertAnnotationArgumentToExpression(value)),
        ),
        false,
      );
      break;
    default:
      return ts.factory.createOmittedExpression();
  }
}

export default class ModelSchemaProcessor {
  readonly #schema: Schema;
  readonly #dependencies: DependencyManager;
  readonly #cwd: string;

  public constructor(schema: Schema, dependencies: DependencyManager, cwd: string) {
    this.#schema = schema;
    this.#dependencies = dependencies;
    this.#cwd = cwd;
  }

  public process(): [TypeNode, TypeNode, Identifier, ArrayLiteralExpression] {
    const unwrappedSchema = (
      isComposedSchema(this.#schema) ? decomposeSchema(this.#schema)[0] : this.#schema
    ) as NonComposedSchema;

    let type: TypeNode;
    let modelType: TypeNode;
    let model: Identifier;
    let args: Expression[] = [];
    if (isReferenceSchema(unwrappedSchema)) {
      [type, modelType, model, args] = this.#processReference(unwrappedSchema);
    } else if (isArraySchema(unwrappedSchema)) {
      [type, modelType, model, args] = this.#processArray(unwrappedSchema);
    } else if (isMapSchema(unwrappedSchema)) {
      [type, modelType, model, args] = this.#processRecord(unwrappedSchema);
    } else if (isStringSchema(unwrappedSchema)) {
      type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword);
      model = this.#getBuiltinFormExport('StringModel');
      modelType = ts.factory.createTypeReferenceNode(model);
    } else if (isNumberSchema(unwrappedSchema)) {
      type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
      model = this.#getBuiltinFormExport('NumberModel');
      modelType = ts.factory.createTypeReferenceNode(model);
    } else if (isIntegerSchema(unwrappedSchema)) {
      type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.NumberKeyword);
      model = this.#getBuiltinFormExport('NumberModel');
      modelType = ts.factory.createTypeReferenceNode(model);
    } else if (isBooleanSchema(unwrappedSchema)) {
      type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.BooleanKeyword);
      model = this.#getBuiltinFormExport('BooleanModel');
      modelType = ts.factory.createTypeReferenceNode(model);
    } else {
      type = ts.factory.createKeywordTypeNode(ts.SyntaxKind.UnknownKeyword);
      model = this.#getBuiltinFormExport('ObjectModel');
      modelType = ts.factory.createTypeReferenceNode(model);
    }

    const optionalArg = isNullableSchema(this.#schema) ? ts.factory.createTrue() : ts.factory.createFalse();

    if (isAnnotatedSchema(unwrappedSchema)) {
      args = [...args, ...this.#getValidators(unwrappedSchema)];
    }

    return [type, modelType, model, ts.factory.createArrayLiteralExpression([optionalArg, ...args])];
  }

  #processReference(schema: ReferenceSchema): [TypeNode, TypeNode, Identifier, Expression[]] {
    const schemaPath = convertReferenceSchemaToPath(schema);
    const typeName = convertReferenceSchemaToSpecifier(schema);
    const typePath = this.#dependencies.paths.createRelativePath(schemaPath, this.#cwd);
    const modelPath = this.#dependencies.paths.createRelativePath(`${schemaPath}Model`, this.#cwd);
    const modelName = `${typeName}Model`;
    const refType =
      this.#dependencies.imports.default.getIdentifier(typePath) ??
      this.#dependencies.imports.default.add(typePath, typeName, true);
    const type = ts.factory.createTypeReferenceNode(refType);
    const model =
      this.#dependencies.imports.default.getIdentifier(modelPath) ??
      this.#dependencies.imports.default.add(modelPath, modelName);
    const modelType = ts.factory.createTypeReferenceNode(model);
    return [type, modelType, model, []];
  }

  #processArray(schema: ArraySchema): [TypeNode, TypeNode, Identifier, Expression[]] {
    const model = this.#getBuiltinFormExport('ArrayModel');
    const [itemType, itemModelType, itemModel, itemArgs] = new ModelSchemaProcessor(
      schema.items,
      this.#dependencies,
      this.#cwd,
    ).process();
    const type = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('ReadonlyArray'), [itemType]);
    const modelType = ts.factory.createTypeReferenceNode(model, [itemType, itemModelType]);
    return [type, modelType, model, [itemModel, itemArgs]];
  }

  #processRecord(schema: MapSchema): [TypeNode, TypeNode, Identifier, Expression[]] {
    const model = this.#getBuiltinFormExport('ObjectModel');
    const [valueType] = new ModelSchemaProcessor(
      schema.additionalProperties as Schema,
      this.#dependencies,
      this.#cwd,
    ).process();
    const type = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('Record'), [
      ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
      valueType,
    ]);
    const modelType = ts.factory.createTypeReferenceNode(model, [type]);
    return [type, modelType, model, []];
  }

  #getValidators(schema: AnnotatedSchema): Expression[] {
    const validators: Expression[] = [];
    schema['x-annotations'].forEach((annotationText: string) => {
      const annotation = parseAnnotation(annotationText);
      const validator = this.#getBuiltinFormExport(annotation.simpleName);
      const newValidator = ts.factory.createNewExpression(
        validator,
        undefined,
        annotation.arguments.map(convertAnnotationArgumentToExpression),
      );
      validators.push(newValidator);
    });
    return validators;
  }

  #getBuiltinFormExport(specifier: string): Identifier {
    const modelPath = this.#dependencies.paths.createBareModulePath('@vaadin/form', false);
    return (
      this.#dependencies.imports.named.getIdentifier(modelPath, specifier) ??
      this.#dependencies.imports.named.add(modelPath, specifier)
    );
  }
}
