import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { template, transform } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ts, { type FunctionDeclaration, type SourceFile } from 'typescript';

export type MethodInfo = Readonly<{
  name: string;
  signalType: string;
}>;

const HILLA_REACT_SIGNALS = '@vaadin/hilla-react-signals';

const NUMBER_SIGNAL_CHANNEL = '$NUMBER_SIGNAL_CHANNEL$';
const CONNECT_CLIENT = '$CONNECT_CLIENT$';

const signalImportPaths = ['com/vaadin/hilla/signals/NumberSignal'];

export default class SignalProcessor {
  readonly #dependencyManager: DependencyManager;
  readonly #owner: Plugin;
  readonly #service: string;
  readonly #methods: MethodInfo[];
  readonly #sourceFile: SourceFile;

  constructor(service: string, methods: MethodInfo[], sourceFile: SourceFile, owner: Plugin) {
    this.#service = service;
    this.#methods = methods;
    this.#sourceFile = sourceFile;
    this.#owner = owner;
    this.#dependencyManager = new DependencyManager(new PathManager({ extension: '.js' }));
    this.#dependencyManager.imports.fromCode(this.#sourceFile);
  }

  process(): SourceFile {
    this.#owner.logger.debug(`Processing signals: ${this.#service}`);
    const { imports } = this.#dependencyManager;
    const numberSignalChannelId = imports.named.add(HILLA_REACT_SIGNALS, 'NumberSignalChannel');

    const [_p, _isType, connectClientId] = imports.default.find((p) => p.includes('connect-client'))!;

    this.#processSignalImports(signalImportPaths);

    const [file] = ts.transform<SourceFile>(this.#sourceFile, [
      ...this.#methods.map((method) =>
        transform<SourceFile>((tsNode) => {
          if (ts.isFunctionDeclaration(tsNode) && tsNode.name?.text === method.name) {
            const body = template(
              `
function dummy() {
  return new ${NUMBER_SIGNAL_CHANNEL}('${this.#service}.${method.name}', ${CONNECT_CLIENT}).signal;
}`,
              (statements) => (statements[0] as FunctionDeclaration).body?.statements,
              [
                transform((node) =>
                  ts.isIdentifier(node) && node.text === NUMBER_SIGNAL_CHANNEL ? numberSignalChannelId : node,
                ),
                transform((node) => (ts.isIdentifier(node) && node.text === CONNECT_CLIENT ? connectClientId : node)),
              ],
            );

            let returnType = tsNode.type;
            if (
              returnType &&
              ts.isTypeReferenceNode(returnType) &&
              'text' in returnType.typeName &&
              returnType.typeName.text === 'Promise'
            ) {
              if (returnType.typeArguments && returnType.typeArguments.length > 0) {
                returnType = returnType.typeArguments[0];
              }
            }

            return ts.factory.createFunctionDeclaration(
              tsNode.modifiers?.filter((modifier) => modifier.kind !== ts.SyntaxKind.AsyncKeyword),
              tsNode.asteriskToken,
              tsNode.name,
              tsNode.typeParameters,
              tsNode.parameters.filter(({ name }) => !(ts.isIdentifier(name) && name.text === 'init')),
              returnType,
              ts.factory.createBlock(body ?? [], false),
            );
          }

          return tsNode;
        }),
      ),
    ]).transformed;

    this.#removeUnusedRequestInitImports(file);

    return createSourceFile(
      [
        ...this.#dependencyManager.imports.toCode(),
        ...file.statements.filter((statement) => !ts.isImportDeclaration(statement)),
      ],
      file.fileName,
    );
  }

  #processSignalImports(signalImports: readonly string[]) {
    const { imports } = this.#dependencyManager;

    signalImports.forEach((signalImport) => {
      const result = imports.default.find((p) => p.includes(signalImport));

      if (result) {
        const [path, _, id] = result;
        imports.default.remove(path);
        imports.named.add(HILLA_REACT_SIGNALS, id.text, true, id);
      }
    });
  }

  #removeUnusedRequestInitImports(file: SourceFile) {
    const transformedFileText = ts.createPrinter().printFile(file);

    const hasNormalEndpointCalls =
      transformedFileText.includes('init?: EndpointRequestInit') && transformedFileText.includes('.call(');

    if (!hasNormalEndpointCalls) {
      this.#dependencyManager.imports.named.remove('@vaadin/hilla-frontend', 'EndpointRequestInit');
    }
  }
}
