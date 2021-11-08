import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile, Statement } from 'typescript';
import ts, { TypeElement } from 'typescript';
import Schema, { EnumSchema, ObjectSchema } from '../../core/Schema.js';
import { convertFullyQualifiedNameToRelativePath, simplifyFullyQualifiedName } from '../../core/utils.js';
import DependencyManager from './DependencyManager.js';
import SchemaProcessor from './SchemaProcessor.js';
import { createSourceFile } from './utils.js';
import type { BackbonePluginContext } from './utils.js';

const exportDefaultModifiers = [
  ts.factory.createModifier(ts.SyntaxKind.ExportKeyword),
  ts.factory.createModifier(ts.SyntaxKind.DefaultKeyword),
];

export class EntityProcessor {
  readonly #component: Schema;
  readonly #context: BackbonePluginContext;
  readonly #dependencies = new DependencyManager();
  readonly #fullyQualifiedName: string;
  readonly #name: string;
  readonly #path: string;

  public constructor(
    name: string,
    component: ReadonlyDeep<OpenAPIV3.ReferenceObject | OpenAPIV3.SchemaObject>,
    context: BackbonePluginContext,
  ) {
    this.#component = Schema.of(component);
    this.#context = context;
    this.#fullyQualifiedName = name;
    this.#name = simplifyFullyQualifiedName(name);
    this.#path = convertFullyQualifiedNameToRelativePath(name);
  }

  public process(): SourceFile {
    this.#context.logger.info(`Processing entity: ${this.#name}`);
    const declaration = this.#component.isEnum() ? this.#processEnum() : this.#processClass();

    const { imports, exports } = this.#dependencies;

    const importStatements = imports.toTS();
    const exportStatement = exports.toTS();

    return createSourceFile(
      [...importStatements, declaration, exportStatement].filter(Boolean) as readonly Statement[],
      this.#path,
    );
  }

  #processClass(): Statement {
    return ts.factory.createInterfaceDeclaration(
      undefined,
      exportDefaultModifiers,
      this.#name,
      undefined,
      undefined,
      this.#processTypeElements(),
    );
  }

  #processEnum(): Statement {
    return ts.factory.createEnumDeclaration(
      undefined,
      exportDefaultModifiers,
      this.#name,
      (this.#component as EnumSchema).members?.map((member) =>
        ts.factory.createEnumMember(member, ts.factory.createStringLiteral(member)),
      ) ?? [],
    );
  }

  #processTypeElements(): readonly TypeElement[] {
    if (!this.#component.isObject()) {
      this.#context.logger.warn(`The component is not an object: ${this.#fullyQualifiedName}`);
      return [];
    }

    if ((this.#component as ObjectSchema).isEmptyObject()) {
      this.#context.logger.warn(`The component has no properties: ${this.#fullyQualifiedName}`);
      return [];
    }

    return Array.from((this.#component as ObjectSchema).properties!, ([name, schema]) => {
      const [type] = new SchemaProcessor(schema, this.#dependencies).process();

      return ts.factory.createPropertySignature(
        undefined,
        name,
        schema.isNullable() ? ts.factory.createToken(ts.SyntaxKind.QuestionToken) : undefined,
        type,
      );
    });
  }
}
