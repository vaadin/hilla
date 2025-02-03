import type { $Refs } from '@apidevtools/swagger-parser';
import type { OpenAPIV3 } from 'openapi-types';
import type { SourceFile } from 'typescript';

type SharedStorage = Readonly<{
  api: OpenAPIV3.Document;
  apiRefs: $Refs;
  sources: SourceFile[];
  pluginStorage: Map<string, unknown>;
  outputDir?: string;
}>;

export default SharedStorage;
