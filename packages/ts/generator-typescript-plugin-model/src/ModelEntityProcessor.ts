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
  ObjectSchema,
} from '@vaadin/generator-typescript-core/Schema.js';
import {
  convertFullyQualifiedNameToRelativePath,
  simplifyFullyQualifiedName,
} from '@vaadin/generator-typescript-core/utils.js';
import DependencyManager from '@vaadin/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/generator-typescript-utils/dependencies/PathManager.js';
import createSourceFile from '@vaadin/generator-typescript-utils/createSourceFile.js';
import { dirname } from 'path/posix';
import type { ModelPluginContext } from './utils.js';
import ModelSchemaProcessor from './ModelSchemaProcessor.js';

const exportDefaultModifiers = [
  ts.factory.createModifier(ts.SyntaxKind.ExportKeyword),
  ts.factory.createModifier(ts.SyntaxKind.DefaultKeyword),
];

export class ModelEntityProcessor {
  readonly #component: Schema;
  readonly #context: ModelPluginContext;
  readonly #fullyQualifiedName: string;
  readonly #name: string;
  readonly #entityName: string;
  readonly #path: string;
  readonly #dependencies: DependencyManager;
  readonly #sourcePaths = new PathManager({ extension: 'ts' });
  #getPropertyModelSymbol?: Identifier = undefined;

  public constructor(name: string, component: Schema, context: ModelPluginContext) {
    this.#component = component;
    this.#context = context;
    this.#fullyQualifiedName = name;
    this.#entityName = simplifyFullyQualifiedName(name);
    this.#name = `${this.#entityName}Model`;
    this.#path = convertFullyQualifiedNameToRelativePath(`${name}Model`);
    this.#dependencies = new DependencyManager(new PathManager({ relativeTo: dirname(this.#path) }));
  }

  public process(): SourceFile {
    this.#context.logger.debug(`Processing model for entity: ${this.#entityName}`);

    const entity = this.#dependencies.imports.default.add(
      this.#dependencies.paths.createRelativePath(convertFullyQualifiedNameToRelativePath(this.#fullyQualifiedName)),
      this.#entityName,
      true,
    );

    const declaration = this.#processExtendedModelClass(this.#component, entity);

    const { imports, exports } = this.#dependencies;
    const importStatements = imports.toCode();
    const exportStatement = exports.toCode();

    return createSourceFile(
      [...importStatements, declaration, ...exportStatement].filter(Boolean) as readonly Statement[],
      this.#sourcePaths.createRelativePath(this.#path),
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
      parent = this.#dependencies.imports.named.add('@vaadin/form', 'ObjectModel');
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

    const fields = this.#processClassElements(schema);

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

  #processClassElements({ required, properties }: ObjectSchema): readonly ClassElement[] {
    const requiredSet = new Set(required);
    return Object.entries(properties || []).map(([name, schema]) => {
      const [, modelType, model, [, ...modelVariableArgs]] = new ModelSchemaProcessor(
        schema,
        this.#dependencies,
      ).process();
      const optional = !requiredSet.has(name);
      const argsArray = ts.factory.createArrayLiteralExpression([
        optional ? ts.factory.createTrue() : ts.factory.createFalse(),
        ...modelVariableArgs,
      ]);

      return ts.factory.createGetAccessorDeclaration(
        undefined,
        undefined,
        ts.factory.createIdentifier(name),
        [],
        modelType,
        ts.factory.createBlock(
          [
            ts.factory.createReturnStatement(
              ts.factory.createAsExpression(
                ts.factory.createCallExpression(
                  ts.factory.createElementAccessExpression(ts.factory.createThis(), this.#getGetPropertyModelSymbol()),
                  undefined,
                  [ts.factory.createStringLiteral(name), model, argsArray],
                ),
                modelType,
              ),
            ),
          ],
          true,
        ),
      );
    });
  }

  #processParentClass(schema: ReferenceSchema): Identifier {
    const { imports, paths } = this.#dependencies;

    const specifier = convertReferenceSchemaToSpecifier(schema);
    const path = convertReferenceSchemaToPath(schema);
    const modelPath = paths.createRelativePath(`${path}Model`);
    const modelSpecifier = `${specifier}Model`;

    return imports.default.add(modelPath, modelSpecifier, false);
  }

  #getGetPropertyModelSymbol(): Identifier {
    this.#getPropertyModelSymbol ||= this.#dependencies.imports.named.add('@vaadin/form', '_getPropertyModel');
    return this.#getPropertyModelSymbol;
  }
}
