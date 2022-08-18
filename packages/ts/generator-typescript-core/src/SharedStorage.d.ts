import { $Refs } from '@apidevtools/swagger-parser';
import { OpenAPIV3 } from 'openapi-types';
import { ReadonlyDeep } from 'type-fest';
import { SourceFile } from 'typescript';

type SharedStorage = Readonly<{
  api: ReadonlyDeep<OpenAPIV3.Document>;
  apiRefs: $Refs;
  sources: SourceFile[];
  pluginStorage: Map<string, unknown>;
  outputDir?: string;
}>;

export default SharedStorage;
