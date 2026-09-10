import ts, { type Expression, type SourceFile } from '@typescript/typescript6';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import { removeUnusedImports } from './utils.js';

const MODELS_MODULE = '@vaadin/hilla-models';

/**
 * The local name the model file gave to the `m` namespace, which the builder
 * chain of every generated model already uses.
 */
function findModelsNamespace(source: SourceFile): ts.Identifier | undefined {
  for (const statement of source.statements) {
    if (
      ts.isImportDeclaration(statement) &&
      ts.isStringLiteral(statement.moduleSpecifier) &&
      statement.moduleSpecifier.text === MODELS_MODULE
    ) {
      const name = statement.importClause?.name;

      if (name) {
        return name;
      }
    }
  }

  return undefined;
}

/**
 * Pins the discriminator to the values the type accepts, e.g.
 * `m.literal("add")`, or a union of them for a subtype that is also the
 * supertype of another one.
 */
function createDiscriminatorModel(m: ts.Identifier, values: readonly string[]): Expression {
  const literal = (value: string) =>
    ts.factory.createCallExpression(ts.factory.createPropertyAccessExpression(m, 'literal'), undefined, [
      ts.factory.createStringLiteral(value),
    ]);

  return values.length === 1
    ? literal(values[0])
    : ts.factory.createCallExpression(
        ts.factory.createPropertyAccessExpression(m, 'union'),
        undefined,
        values.map(literal),
      );
}

export class ModelFixProcessor {
  readonly #source: SourceFile;
  readonly #discriminatorPropertyName: string;
  readonly #values: readonly string[];

  constructor(source: SourceFile, discriminatorPropertyName: string, values: readonly string[]) {
    this.#source = source;
    this.#discriminatorPropertyName = discriminatorPropertyName;
    this.#values = values;
  }

  process(): SourceFile {
    const m = findModelsNamespace(this.#source);

    if (!m || this.#values.length === 0) {
      return this.#source;
    }

    const statements = this.#source.statements.map((statement) => {
      if (!ts.isVariableStatement(statement)) {
        return statement;
      }

      const declarations = statement.declarationList.declarations.map((declaration) =>
        declaration.initializer
          ? ts.factory.updateVariableDeclaration(
              declaration,
              declaration.name,
              declaration.exclamationToken,
              declaration.type,
              this.#rewrite(declaration.initializer, m),
            )
          : declaration,
      );

      return ts.factory.updateVariableStatement(
        statement,
        statement.modifiers,
        ts.factory.updateVariableDeclarationList(statement.declarationList, declarations),
      );
    });

    return createSourceFile(removeUnusedImports(statements), this.#source.fileName);
  }

  /**
   * Walks the `m.object(…).property(…)…build()` chain, which nests to the left,
   * and replaces the model of the discriminator property.
   */
  #rewrite(node: Expression, m: ts.Identifier): Expression {
    if (!ts.isCallExpression(node) || !ts.isPropertyAccessExpression(node.expression)) {
      return node;
    }

    const access = node.expression;
    const target = ts.factory.updatePropertyAccessExpression(access, this.#rewrite(access.expression, m), access.name);

    const [key] = node.arguments;
    const isDiscriminator =
      access.name.text === 'property' &&
      node.arguments.length === 2 &&
      ts.isStringLiteral(key) &&
      key.text === this.#discriminatorPropertyName;

    return ts.factory.updateCallExpression(
      node,
      target,
      node.typeArguments,
      isDiscriminator ? [key, createDiscriminatorModel(m, this.#values)] : node.arguments,
    );
  }
}
