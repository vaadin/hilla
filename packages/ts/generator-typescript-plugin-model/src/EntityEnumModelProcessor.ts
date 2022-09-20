import type DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import ts, { ClassDeclaration } from 'typescript';
import { DependencyData, importBuiltInFormModel } from './utils.js';

export class EntityEnumModelProcessor {
  readonly #dependencies: DependencyManager;
  readonly #entity: DependencyData;
  readonly #fullyQualifiedName: string;
  readonly #model: DependencyData;

  public constructor(name: string, dependencies: DependencyManager, entity: DependencyData, model: DependencyData) {
    this.#dependencies = dependencies;
    this.#entity = entity;
    this.#fullyQualifiedName = name;
    this.#model = model;
  }

  public process(): ClassDeclaration {
    const enumModel = importBuiltInFormModel('EnumModel', this.#dependencies);
    const enumPropertySymbol = this.#dependencies.imports.named.add('@hilla/form', '_enum');

    return ts.factory.createClassDeclaration(
      undefined,
      undefined,
      this.#model.id,
      undefined,
      [
        ts.factory.createHeritageClause(ts.SyntaxKind.ExtendsKeyword, [
          ts.factory.createExpressionWithTypeArguments(enumModel, [
            ts.factory.createTypeQueryNode(this.#entity.id, undefined),
          ]),
        ]),
      ],
      [
        ts.factory.createPropertyDeclaration(
          undefined,
          [ts.factory.createModifier(ts.SyntaxKind.ReadonlyKeyword)],
          ts.factory.createComputedPropertyName(enumPropertySymbol),
          undefined,
          undefined,
          this.#entity.id,
        ),
      ],
    );
  }
}
