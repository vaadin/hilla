import type { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile } from 'typescript';

type TypeScriptFile = Readonly<{
  path: string;
  source: SourceFile;
}>;

type SharedStorage = Readonly<{
  files: Set<TypeScriptFile>;
  openAPI: ReadonlyDeep<OpenAPIV3.Document>;
  pluginStorage: Map<string, unknown>;
}>;

export default SharedStorage;
