import { $Refs } from '@apidevtools/swagger-parser';
import { OpenAPIV3 } from 'openapi-types';
import { ReadonlyDeep } from 'type-fest';
import { Statement } from 'typescript';

export type SourceMap = Map<string, Statement[]>;

type SharedStorage = Readonly<{
  api: ReadonlyDeep<OpenAPIV3.Document>;
  apiRefs: $Refs;
  sources: SourceMap;
  pluginStorage: Map<string, unknown>;
}>;

export default SharedStorage;
