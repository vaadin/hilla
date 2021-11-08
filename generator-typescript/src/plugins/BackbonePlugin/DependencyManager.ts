import type { Identifier, NamedImports, Statement } from 'typescript';
import ts from 'typescript';

const collator = new Intl.Collator('en', { sensitivity: 'case' });

export class ExportManager {
  readonly #exports = new Map<string, Identifier>();

  public register(exportName: string, uniqueIdentifier?: Identifier): Identifier {
    const id = uniqueIdentifier ?? ts.factory.createUniqueName(exportName);
    this.#exports.set(exportName, id);

    return id;
  }

  public toTS(): Statement | undefined {
    if (this.#exports.size === 0) {
      return undefined;
    }

    const keys = [...this.#exports.keys()];
    keys.sort(collator.compare);

    return ts.factory.createExportDeclaration(
      undefined,
      undefined,
      false,
      ts.factory.createNamedExports(
        keys.map((key) =>
          ts.factory.createExportSpecifier(false, this.#exports.get(key), ts.factory.createIdentifier(key)),
        ),
      ),
      undefined,
    );
  }
}

export class ImportManager {
  readonly #imports = new Map<string, Map<string, Identifier>>();

  public getIdentifier(specifier: string, path: string): Identifier | undefined {
    return this.#imports.get(path)?.get(specifier);
  }

  public register(specifier: string, path: string): Identifier {
    const id = ts.factory.createUniqueName(specifier);

    if (this.#imports.has(path)) {
      this.#imports.get(path)!.set(specifier, id);
    } else {
      this.#imports.set(path, new Map([[specifier, id]]));
    }

    return id;
  }

  public toTS(): readonly Statement[] {
    const paths = [...this.#imports.keys()];
    paths.sort(collator.compare);

    return paths.map((path) => {
      const specifiers = this.#imports.get(path)!;

      let singleImport: Identifier | undefined;
      let namedImports: NamedImports | undefined;

      if (specifiers.size > 1) {
        const names = [...specifiers.keys()];
        names.sort(collator.compare);

        namedImports = ts.factory.createNamedImports(
          names.map((name) =>
            ts.factory.createImportSpecifier(false, ts.factory.createIdentifier(name), specifiers.get(name)!),
          ),
        );
      } else {
        [singleImport] = [...specifiers.values()];
      }

      return ts.factory.createImportDeclaration(
        undefined,
        undefined,
        ts.factory.createImportClause(true, singleImport, namedImports),
        ts.factory.createStringLiteral(path),
      );
    });
  }
}

export default class DependencyManager {
  readonly #exports: ExportManager = new ExportManager();
  readonly #imports: ImportManager = new ImportManager();

  public get exports(): ExportManager {
    return this.#exports;
  }

  public get imports(): ImportManager {
    return this.#imports;
  }
}
