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
import type { Expression, Identifier, TypeNode } from 'typescript';
import ts from 'typescript';

type AnnotatedSchema = NonComposedRegularSchema & Readonly<{ 'x-annotations': ReadonlyArray<string> }>;

function isAnnotatedSchema(schema: Schema): schema is AnnotatedSchema {
  return isNonComposedRegularSchema(schema) && 'x-annotations' in schema;
}

type AnnotationPrimitiveAttribute = boolean | number | string;
type AnnotationNamedAttributes = Readonly<{
  [name: string]: AnnotationPrimitiveAttribute;
}>;
type Annotation = Readonly<{
  simpleName: string;
  attributes?: AnnotationNamedAttributes;
}>;

export type ModelSchemaProcessorResult = Readonly<
  [type: TypeNode, modelType: TypeNode, model: Identifier, args: readonly Expression[]]
>;

const keywords: Record<string, AnnotationPrimitiveAttribute> = { true: true, false: false };

function parseAttribute(attributeText: string): AnnotationPrimitiveAttribute {
  if (attributeText in keywords) {
    return keywords[attributeText];
  }

  if (attributeText.startsWith('"') && attributeText.endsWith('"')) {
    return attributeText.slice(1, attributeText.length - 1).replace(/\\\\/g, '\\');
  }

  const number = Number(attributeText);
  if (!Number.isNaN(number) || attributeText.toLowerCase() === 'nan') {
    return number;
  }

  throw new PluginError(`Unable to parse annotation argument "${attributeText}"`);
}

function parseAttributes(attributesText: string): AnnotationNamedAttributes {
  attributesText = attributesText.trim();
  if (attributesText.startsWith('{') && attributesText.endsWith('}')) {
    const namedList = attributesText.slice(1, attributesText.length - 1);
    const attributes: AnnotationNamedAttributes = namedList.split(',').reduce((record, pairText) => {
      const [key, valueText] = pairText.split(':');
      record[key.trim()] = parseAttribute(valueText);
      return record;
    }, {} as Record<string, AnnotationPrimitiveAttribute>);
    return attributes;
  }

  return { value: parseAttribute(attributesText) };
}

function parseAnnotation(annotationText: string): Annotation {
  const [, simpleName, argumentsText] = /^(\w+)\((.*)\)$/.exec(annotationText) || [];
  if (simpleName === undefined) {
    throw new PluginError(`Unknown annotation format when processing "${annotationText}"`);
  }

  if (argumentsText !== undefined && argumentsText.trim() !== '') {
    const attributes = parseAttributes(argumentsText);
    return { simpleName, attributes };
  }

  return { simpleName };
}

function convertAttribute(attribute: AnnotationPrimitiveAttribute): Expression {
  switch (typeof attribute) {
    case 'boolean':
      return attribute ? ts.factory.createTrue() : ts.factory.createFalse();
    case 'number':
      return ts.factory.createNumericLiteral(attribute);
    case 'string':
      return ts.factory.createStringLiteral(attribute);
    default:
      return ts.factory.createOmittedExpression();
  }
}

function convertNamedAttributes(attributes: AnnotationNamedAttributes): Expression {
  const attributeEntries = Object.entries(attributes);
  if (attributeEntries.length === 1 && attributeEntries[0][0] === 'value') {
    return convertAttribute(attributeEntries[0][1]);
  }

  return ts.factory.createObjectLiteralExpression(
    attributeEntries.map(([key, value]) => ts.factory.createPropertyAssignment(key, convertAttribute(value))),
    false,
  );
}

export default class ModelSchemaProcessor {
  readonly #schema: Schema;
  readonly #dependencies: DependencyManager;

  public constructor(schema: Schema, dependencies: DependencyManager) {
    this.#schema = schema;
    this.#dependencies = dependencies;
  }

  public process(): ModelSchemaProcessorResult {
    const unwrappedSchema = (
      isComposedSchema(this.#schema) ? decomposeSchema(this.#schema)[0] : this.#schema
    ) as NonComposedSchema;

    let type: TypeNode;
    let modelType: TypeNode;
    let model: Identifier;
    let args: readonly Expression[] = [];
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
    } else if (isNumberSchema(unwrappedSchema) || isIntegerSchema(unwrappedSchema)) {
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

    return [type, modelType, model, [optionalArg, ...args]];
  }

  #processReference(schema: ReferenceSchema): ModelSchemaProcessorResult {
    const schemaPath = convertReferenceSchemaToPath(schema);
    const typeName = convertReferenceSchemaToSpecifier(schema);
    const { paths, imports } = this.#dependencies;
    const typePath = paths.createRelativePath(schemaPath);
    const modelPath = paths.createRelativePath(`${schemaPath}Model`);
    const modelName = `${typeName}Model`;
    const refType = imports.default.getIdentifier(typePath) ?? imports.default.add(typePath, typeName, true);
    const type = ts.factory.createTypeReferenceNode(refType);
    const model = imports.default.getIdentifier(modelPath) ?? imports.default.add(modelPath, modelName);
    const modelType = ts.factory.createTypeReferenceNode(model);
    return [type, modelType, model, []];
  }

  #processArray(schema: ArraySchema): ModelSchemaProcessorResult {
    const model = this.#getBuiltinFormExport('ArrayModel');
    const [itemType, itemModelType, itemModel, itemArgs] = new ModelSchemaProcessor(
      schema.items,
      this.#dependencies,
    ).process();
    const type = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('ReadonlyArray'), [itemType]);
    const modelType = ts.factory.createTypeReferenceNode(model, [itemType, itemModelType]);
    return [type, modelType, model, [itemModel, ts.factory.createArrayLiteralExpression(itemArgs)]];
  }

  #processRecord(schema: MapSchema): ModelSchemaProcessorResult {
    const model = this.#getBuiltinFormExport('ObjectModel');

    let valueType: TypeNode;
    if (typeof schema.additionalProperties === 'boolean') {
      valueType = ts.factory.createKeywordTypeNode(ts.SyntaxKind.AnyKeyword);
    } else {
      const valueSchema: Schema = schema.additionalProperties;
      [valueType] = new ModelSchemaProcessor(valueSchema, this.#dependencies).process();
    }

    const type = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('Record'), [
      ts.factory.createKeywordTypeNode(ts.SyntaxKind.StringKeyword),
      valueType,
    ]);
    const modelType = ts.factory.createTypeReferenceNode(model, [type]);
    return [type, modelType, model, []];
  }

  #getValidators(schema: AnnotatedSchema): readonly Expression[] {
    return schema['x-annotations'].map((annotationText: string) => this.#getValidator(parseAnnotation(annotationText)));
  }

  #getValidator(annotation: Annotation): Expression {
    const validator = this.#getBuiltinFormExport(annotation.simpleName);
    const attributeArgs = annotation.attributes !== undefined ? [convertNamedAttributes(annotation.attributes)] : [];
    return ts.factory.createNewExpression(validator, undefined, attributeArgs);
  }

  #getBuiltinFormExport(specifier: string): Identifier {
    const { imports, paths } = this.#dependencies;
    const modelPath = paths.createBareModulePath('@vaadin/form', false);
    return imports.named.getIdentifier(modelPath, specifier) ?? imports.named.add(modelPath, specifier);
  }
}
