import ts, {
  type Node,
  type SourceFile,
  type Statement,
  type TransformationContext,
  type TransformerFactory,
} from 'typescript';

export type TemplateSelector<T> = (statements: readonly Statement[]) => T;

export function template(
  code: string,
  transformers?: ReadonlyArray<TransformerFactory<SourceFile>>,
): readonly Statement[];
export function template<T>(
  code: string,
  selector: TemplateSelector<T>,
  transformers?: ReadonlyArray<TransformerFactory<SourceFile>>,
): T;
export function template<T>(
  code: string,
  selectorOrTransformers?: ReadonlyArray<TransformerFactory<SourceFile>> | TemplateSelector<T>,
  transformers?: ReadonlyArray<TransformerFactory<SourceFile>>,
): T | readonly Statement[] {
  let selector: TemplateSelector<T> | undefined;

  if (Array.isArray(selectorOrTransformers)) {
    // eslint-disable-next-line no-param-reassign
    transformers = selectorOrTransformers;
  } else {
    selector = selectorOrTransformers as TemplateSelector<T>;
  }

  let sourceFile = ts.createSourceFile('f.ts', code, ts.ScriptTarget.Latest, false);

  if (transformers) {
    [sourceFile] = ts.transform<SourceFile>(
      sourceFile,
      transformers as Array<TransformerFactory<SourceFile>>,
    ).transformed;
  }

  return selector?.(sourceFile.statements) ?? sourceFile.statements;
}

export function transform<T extends Node>(transformer: (node: Node) => Node): TransformerFactory<T> {
  return (context: TransformationContext) => (root: T) => {
    const visitor = (node: Node): Node => ts.visitEachChild(transformer(node), visitor, context);
    return ts.visitEachChild(root, visitor, context);
  };
}
