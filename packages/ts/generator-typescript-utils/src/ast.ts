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
  selectorOrTransformers?: TemplateSelector<T> | ReadonlyArray<TransformerFactory<SourceFile>>,
  transformers?: ReadonlyArray<TransformerFactory<SourceFile>>,
) {
  let selector: TemplateSelector<T> | undefined;

  if (Array.isArray(selectorOrTransformers)) {
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

export function transform<T extends Node>(transformer: (node: Node) => Node | undefined): TransformerFactory<T> {
  return (context: TransformationContext) => (root: T) => {
    const visitor = (node: Node): Node | undefined => ts.visitEachChild(transformer(node), visitor, context);
    return ts.visitNode(root, visitor);
  };
}
