/*
 * Adapted from `tsc-template` (https://github.com/ausginer/tsc-template),
 * © 2024 Vlad Rindevich, Apache-2.0 licensed.
 *
 * Inlined here so Hilla packages don't carry an external dependency whose
 * peerDependency on TypeScript lags behind ours.
 */
import {
  createSourceFile,
  forEachChild,
  isIdentifier,
  type Node,
  ScriptKind,
  ScriptTarget,
  type SourceFile,
  transform,
  type TransformerFactory,
  visitEachChild,
  type VisitResult,
} from 'typescript';

export type Transformer = (node: Node) => VisitResult<Node | undefined>;

export function createTransformer(transformer: Transformer): TransformerFactory<SourceFile> {
  return (context) => (root) => {
    const visitor = (node: Node): VisitResult<Node | undefined> => {
      const transformed = transformer(node);
      if (transformed !== node) {
        return transformed;
      }
      return visitEachChild(transformed, visitor, context);
    };
    return visitEachChild(root, visitor, context);
  };
}

const $templateResult: unique symbol = Symbol('TemplateResult');

export type TemplateResult = Readonly<{
  brand: typeof $templateResult;
  node: Node;
  source: SourceFile;
}>;

function isTemplateResult(value: unknown): value is TemplateResult {
  return typeof value === 'object' && value !== null && 'brand' in value && value.brand === $templateResult;
}

function extractCodePart(str: string): string {
  const [, startResult = str] = /\/\*\*\s*@START\s*\*\/([\s\S]*)/iu.exec(str) ?? [];
  const [, endResult = startResult] = /([\s\S]*?)\/\*\*\s*@END\s*\*\//iu.exec(startResult) ?? [];
  return endResult.trim();
}

function isInjectedNode(node: Node): boolean {
  return node.pos < 0 || node.end < 0;
}

function findBestNode(file: SourceFile): Node {
  const codePart = extractCodePart(file.getText());
  let latest: Node = file;
  const find = (node: Node): Node | undefined => {
    if (!isInjectedNode(node) && node.getText(file) === codePart) {
      latest = node;
    } else if (latest !== file) {
      return latest;
    }
    return forEachChild(node, find);
  };
  return forEachChild(file, find) ?? latest;
}

export default function ast(
  parts: TemplateStringsArray,
  ...fillers: ReadonlyArray<TemplateResult | Node | string | null | undefined>
): TemplateResult {
  let code = '';
  const transformers: Array<TransformerFactory<SourceFile>> = [];

  for (let i = 0; i < parts.length; i++) {
    const filler = fillers[i];
    code += parts[i];
    if (filler != null) {
      if (typeof filler === 'string') {
        code += filler;
      } else {
        const id = `$${crypto.randomUUID().replaceAll('-', '_')}`;
        code += id;
        transformers.push(
          createTransformer((n) =>
            isIdentifier(n) && n.text === id ? (isTemplateResult(filler) ? filler.node : filler) : n,
          ),
        );
      }
    }
  }

  if (code.includes('%{') || code.includes('}%')) {
    code = code.replaceAll('%{', '/** @START */').replaceAll('}%', '/** @END */');
  }

  if (
    code.indexOf('/** @START */') !== code.lastIndexOf('/** @START */') ||
    code.indexOf('/** @END */') !== code.lastIndexOf('/** @END */')
  ) {
    throw new Error('Only one set of code extractors is allowed: %{ ... }% or /** @START */ ... /** @END */');
  }

  const [source] = transform(
    createSourceFile('', code, ScriptTarget.Latest, false, ScriptKind.TSX),
    transformers,
  ).transformed;
  return { brand: $templateResult, node: findBestNode(source), source };
}
