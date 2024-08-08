import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { template, transform } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ts, { type FunctionDeclaration, type SourceFile } from 'typescript';

const HILLA_REACT_SIGNALS = '@vaadin/hilla-react-signals';

const NUMBER_SIGNAL_CHANNEL = '$NUMBER_SIGNAL_CHANNEL$';
const CONNECT_CLIENT = '$CONNECT_CLIENT$';

const signalImportPaths = ['com/vaadin/hilla/signals/NumberSignal'];

export default class SignalProcessor {
  readonly #dependencyManager: DependencyManager;
  readonly #owner: Plugin;
  readonly #service: string;
  readonly #methods: Map<string, string>;
  readonly #sourceFile: SourceFile;

  constructor(service: string, methods: Map<string, string>, sourceFile: SourceFile, owner: Plugin) {
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

    const [, connectClientId] = imports.default.iter().find(([path]) => path.includes('connect-client'))!;

    this.#processSignalImports(signalImportPaths);
    const initTypeId = imports.named.getIdentifier('@vaadin/hilla-frontend', 'EndpointRequestInit');
    let initTypeUsageCount = 0;

    const [file] = ts.transform<SourceFile>(this.#sourceFile, [
      transform((tsNode) => {
        if (ts.isFunctionDeclaration(tsNode) && tsNode.name && this.#methods.has(tsNode.name.text)) {
          const methodName = tsNode.name.text;

          const body = template(
            `
function dummy() {
  return new ${NUMBER_SIGNAL_CHANNEL}('${this.#service}.${methodName}', ${CONNECT_CLIENT}).signal;
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
      transform((tsNode) => {
        if (ts.isFunctionDeclaration(tsNode)) {
          if (
            !(tsNode.name && this.#methods.has(tsNode.name.text)) &&
            tsNode.parameters.some((p) => p.type && ts.isTypeReferenceNode(p.type) && p.type.typeName === initTypeId)
          ) {
            initTypeUsageCount += 1;
          }
        }
        return tsNode;
      }),
    ]).transformed;

    if (initTypeUsageCount === 0) {
      imports.named.remove('@vaadin/hilla-frontend', 'EndpointRequestInit');
    }

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
      const result = imports.default.iter().find(([path]) => path.includes(signalImport));

      if (result) {
        const [path, id] = result;
        imports.default.remove(path);
        imports.named.add(HILLA_REACT_SIGNALS, id.text, true, id);
      }
    });
  }
}
