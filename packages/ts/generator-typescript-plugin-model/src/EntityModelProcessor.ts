import type { Schema } from '@hilla/generator-typescript-core/Schema.js';
import { decomposeSchema, isComposedSchema, isEnumSchema } from '@hilla/generator-typescript-core/Schema.js';
import {
  convertFullyQualifiedNameToRelativePath,
  simplifyFullyQualifiedName,
} from '@hilla/generator-typescript-core/utils.js';
import createSourceFile from '@hilla/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@hilla/generator-typescript-utils/dependencies/DependencyManager.js';
import PathManager from '@hilla/generator-typescript-utils/dependencies/PathManager.js';
import { dirname } from 'path/posix';
import type { SourceFile, Statement } from 'typescript';
import { EntityClassModelProcessor } from './EntityClassModelProcessor.js';
import { EntityEnumModelProcessor } from './EntityEnumModelProcessor.js';
import type { Context, DependencyData } from './utils.js';

export class EntityModelProcessor {
  readonly #component: Schema;
  readonly #context: Context;
  readonly #dependencies: DependencyManager;
  readonly #entity: DependencyData;
  readonly #fullyQualifiedName: string;
  readonly #model: DependencyData;
  readonly #sourcePaths = new PathManager({ extension: 'ts' });

  public constructor(name: string, component: Schema, context: Context) {
    this.#fullyQualifiedName = name;
    this.#component = component;
    this.#context = context;

    const entityName = simplifyFullyQualifiedName(name);
    const entityPath = convertFullyQualifiedNameToRelativePath(name);

    const modelName = `${entityName}Model`;
    const modelPath = `${entityPath}Model`;
    this.#dependencies = new DependencyManager(new PathManager({ relativeTo: dirname(modelPath) }));

    const { exports, imports, paths } = this.#dependencies;

    this.#model = {
      id: exports.default.set(modelName),
      path: modelPath,
    };

    this.#entity = {
      id: imports.default.add(paths.createRelativePath(entityPath), entityName, true),
      path: entityPath,
    };
  }

  public process(): SourceFile {
    this.#context.owner.logger.debug(`Processing model for entity: ${this.#entity.id.text}`);

    const schema = isComposedSchema(this.#component) ? decomposeSchema(this.#component)[0] : this.#component;

    const declaration = isEnumSchema(schema)
      ? new EntityEnumModelProcessor(this.#fullyQualifiedName, this.#dependencies, this.#entity, this.#model).process()
      : new EntityClassModelProcessor(
          this.#fullyQualifiedName,
          this.#component,
          this.#dependencies,
          this.#entity,
          this.#model,
          this.#context,
        ).process();

    const { imports, exports } = this.#dependencies;
    const importStatements = imports.toCode();
    const exportStatement = exports.toCode();

    return createSourceFile(
      [...importStatements, declaration, ...exportStatement].filter(Boolean) as readonly Statement[],
      this.#sourcePaths.createRelativePath(this.#model.path),
    );
  }
}
