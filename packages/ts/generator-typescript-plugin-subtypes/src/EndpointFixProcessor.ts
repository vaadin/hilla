import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import ts from 'typescript';
import { propertyNameToString } from './utils';

export class EndpointFixProcessor {
  readonly #dependencies = new DependencyManager(new PathManager({ extension: '.js' }));
  readonly #source: ts.SourceFile;
  readonly #unions: Map<string, string>;

  constructor(source: ts.SourceFile, unions: Map<string, string>) {
    this.#source = source;
    this.#unions = unions;
    this.#dependencies.imports.fromCode(source);
  }

  process(): ts.SourceFile {
    let oldIdentifier: ts.Identifier | undefined;
    let newIdentifier: ts.Identifier | undefined;

    this.#dependencies.imports.toCode().forEach((statement) => {
      if (ts.isImportDeclaration(statement)) {
        const original = (statement.moduleSpecifier as ts.StringLiteral).text;

        const replacement = this.#unions.get(original);

        if (replacement && statement.importClause?.name) {
          oldIdentifier = this.#dependencies.imports.default.getIdentifier(original);
          this.#dependencies.imports.default.delete(original);
          newIdentifier = this.#dependencies.imports.default.add(
            replacement,
            `${propertyNameToString(statement.importClause.name)!}Union`,
            true,
          );
        }
      }
    });

    const otherStatements = this.#source.statements
      .filter((statement) => !ts.isImportDeclaration(statement))
      .map((statement) => {
        if (ts.isFunctionDeclaration(statement)) {
          // replace all occurrences of oldIdentifier with newIdentifier
        }
        return statement;
      });
    const updatedStatements: readonly ts.Statement[] = [...this.#dependencies.imports.toCode(), ...otherStatements];

    return createSourceFile(updatedStatements, this.#source.fileName);
  }
}
