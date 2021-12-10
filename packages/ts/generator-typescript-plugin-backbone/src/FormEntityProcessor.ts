import type { ClassElement, Identifier, SourceFile, Statement } from 'typescript';
import ts, { ClassDeclaration } from 'typescript';
import type { ReferenceSchema, Schema } from '@vaadin/generator-typescript-core/Schema.js';
import {
  convertReferenceSchemaToPath,
  convertReferenceSchemaToSpecifier,
  decomposeSchema,
  isComposedSchema,
  isEmptyObject,
  isObjectSchema,
  isReferenceSchema,
  NonEmptyObjectSchema,
} from '@vaadin/generator-typescript-core/Schema.js';
import {
  convertFullyQualifiedNameToRelativePath,
  simplifyFullyQualifiedName,
} from '@vaadin/generator-typescript-core/utils.js';
import DependencyManager from './DependencyManager.js';
import type { BackbonePluginContext } from './utils.js';
import { createSourceFile } from './utils.js';
import ModelSchemaProcessor from './ModelSchemaProcessor.js';

const exportDefaultModifiers = [
  ts.factory.createModifier(ts.SyntaxKind.ExportKeyword),
  ts.factory.createModifier(ts.SyntaxKind.DefaultKeyword),
];

export class FormEntityProcessor {
  readonly #component: Schema;
  readonly #context: BackbonePluginContext;
  readonly #dependencies = new DependencyManager();
  readonly #fullyQualifiedName: string;
  readonly #name: string;
  readonly #entityName: string;
  readonly #path: string;
  #getPropertyModelSymbol?: Identifier = undefined;

  public constructor(name: string, component: Schema, context: BackbonePluginContext) {
    this.#component = component;
    this.#context = context;
    this.#fullyQualifiedName = name;
    this.#entityName = simplifyFullyQualifiedName(name);
    this.#name = `${this.#entityName}Model`;
    this.#path = convertFullyQualifiedNameToRelativePath(`${name}Model`);
  }

  public process(): SourceFile {
    this.#context.logger.debug(`Processing model for entity: ${this.#entityName}`);

    const entity = this.#dependencies.imports.register(
      this.#entityName,
      convertFullyQualifiedNameToRelativePath(this.#fullyQualifiedName),
    );

    const declaration = this.#processExtendedModelClass(this.#component, entity);

    const { imports, exports } = this.#dependencies;
    const importStatements = imports.toTS();
    const exportStatement = exports.toTS();

    return createSourceFile(
      [...importStatements, declaration, exportStatement].filter(Boolean) as readonly Statement[],
      this.#path,
    );
  }

  #processExtendedModelClass(schema: Schema, entity: Identifier): Statement | undefined {
    const { logger } = this.#context;

    let entitySchema = schema;
    let parent;

    if (isComposedSchema(schema)) {
      const decomposed = decomposeSchema(schema);

      if (decomposed.length > 2) {
        logger.error(schema, `The schema for a class component ${this.#fullyQualifiedName} is broken.`);
        return undefined;
      }

      const [parentSchema, childSchema] = decomposed;

      if (!isReferenceSchema(parentSchema)) {
        logger.error(parent, 'Only reference schema allowed for parent class');
        return undefined;
      }

      entitySchema = childSchema;
      parent = this.#processParentClass(parentSchema);
    } else {
      const parentSpecifier = 'ObjectModel';
      const parentPath = '@vaadin/form';
      parent = this.#dependencies.imports.register(parentSpecifier, parentPath);
    }

    return this.#processModelClass(entitySchema, entity, parent);
  }

  #processModelClass(schema: Schema, entity: Identifier, parent: Identifier): ClassDeclaration | undefined {
    if (!isObjectSchema(schema)) {
      this.#context.logger.error(schema, `The component is not an object: ${this.#fullyQualifiedName}`);
      return undefined;
    }

    if (isEmptyObject(schema)) {
      this.#context.logger.error(`The component has no properties: ${this.#fullyQualifiedName}`);
      return undefined;
    }

    const typeT = ts.factory.createIdentifier('T');
    const modelTypeParameters = ts.factory.createTypeParameterDeclaration(
      typeT,
      ts.factory.createTypeReferenceNode(entity),
      ts.factory.createTypeReferenceNode(entity),
    );

    const emptyValueElement = ts.factory.createPropertyDeclaration(
      undefined,
      [ts.factory.createModifier(ts.SyntaxKind.StaticKeyword)],
      'createEmptyValue',
      undefined,
      ts.factory.createFunctionTypeNode(undefined, [], ts.factory.createTypeReferenceNode(entity)),
      undefined,
    );

    const fields = this.#processClassElements(schema as NonEmptyObjectSchema);

    return ts.factory.createClassDeclaration(
      undefined,
      exportDefaultModifiers,
      this.#name,
      [modelTypeParameters],
      [
        ts.factory.createHeritageClause(ts.SyntaxKind.ExtendsKeyword, [
          ts.factory.createExpressionWithTypeArguments(parent, [ts.factory.createTypeReferenceNode(typeT)]),
        ]),
      ],
      [emptyValueElement, ...fields],
    );
  }

  #processClassElements({ properties }: NonEmptyObjectSchema): readonly ClassElement[] {
    return Object.entries(properties).map(([name, schema]) => {
      const [model, , argsArray] = new ModelSchemaProcessor(schema, this.#dependencies).process();

      return ts.factory.createGetAccessorDeclaration(
        undefined,
        undefined,
        ts.factory.createIdentifier(name),
        [],
        ts.factory.createTypeReferenceNode(model),
        ts.factory.createBlock([
          ts.factory.createReturnStatement(
            ts.factory.createCallExpression(
              ts.factory.createElementAccessExpression(ts.factory.createThis(), this.#getGetPropertyModelSymbol()),
              undefined,
              [ts.factory.createStringLiteral(name), model, argsArray],
            ),
          ),
        ]),
      );
    });
  }

  #processParentClass(schema: ReferenceSchema): Identifier {
    const specifier = convertReferenceSchemaToSpecifier(schema);
    const path = convertReferenceSchemaToPath(schema);
    const modelPath = `${path}Model`;
    const modelSpecifier = `${specifier}Model`;

    return this.#dependencies.imports.register(modelSpecifier, modelPath, true);
  }

  #getGetPropertyModelSymbol(): Identifier {
    this.#getPropertyModelSymbol ||= this.#dependencies.imports.register('_getPropertyModel', '@vaadin/form');
    return this.#getPropertyModelSymbol;
  }
}
