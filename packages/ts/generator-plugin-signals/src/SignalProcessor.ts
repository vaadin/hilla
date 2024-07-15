import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { template, transform } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ts, { type CallExpression, type FunctionDeclaration, type ReturnStatement, type SourceFile } from 'typescript';

export type MethodInfo = Readonly<{
  name: string;
  signalType: string;
}>;

const ENDPOINT_CALL_EXPRESSION = '$ENDPOINT_CALL_EXPRESSION$';
const NUMBER_SIGNAL_QUEUE = '$NUMBER_SIGNAL_QUEUE$';
const SIGNALS_HANDLER = '$SIGNALS_HANDLER$';
const CONNECT_CLIENT = '$CONNECT_CLIENT$';
const HILLA_REACT_SIGNALS = '@vaadin/hilla-react-signals';
const ENDPOINTS = 'Frontend/generated/endpoints.js';

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
    const numberSignalQueueId = imports.named.add(HILLA_REACT_SIGNALS, 'NumberSignalQueue');
    const signalHandlerId = imports.named.add(ENDPOINTS, 'SignalsHandler');

    const [_p, _isType, connectClientId] = imports.default.find((p) => p.includes('connect-client'))!;

    this.#processNumberSignalImport('com/vaadin/hilla/signals/NumberSignal');

    const [file] = ts.transform<SourceFile>(this.#sourceFile, [
      ...this.#methods.map((method) =>
        transform<SourceFile>((node) => {
          if (ts.isFunctionDeclaration(node) && node.name?.text === method.name) {
            const callExpression = (node.body?.statements[0] as ReturnStatement).expression as CallExpression;
            const body = template(
              `
function dummy() {
  const sharedSignal = await ${ENDPOINT_CALL_EXPRESSION};
  const queueDescriptor = {
    id: sharedSignal.id,
    subscribe: ${SIGNALS_HANDLER}.subscribe,
    publish: ${SIGNALS_HANDLER}.update,
  };
  const valueLog = new ${NUMBER_SIGNAL_QUEUE}(queueDescriptor, ${CONNECT_CLIENT});
  return valueLog.getRoot();
}`,
              (statements) => (statements[0] as FunctionDeclaration).body?.statements,
              [
                transform((node) =>
                  ts.isIdentifier(node) && node.text === ENDPOINT_CALL_EXPRESSION ? callExpression : node,
                ),
                transform((node) =>
                  ts.isIdentifier(node) && node.text === NUMBER_SIGNAL_QUEUE ? numberSignalQueueId : node,
                ),
                transform((node) => (ts.isIdentifier(node) && node.text === SIGNALS_HANDLER ? signalHandlerId : node)),
                transform((node) => (ts.isIdentifier(node) && node.text === CONNECT_CLIENT ? connectClientId : node)),
              ],
            );

            return ts.factory.createFunctionDeclaration(
              node.modifiers,
              node.asteriskToken,
              node.name,
              node.typeParameters,
              node.parameters,
              node.type,
              ts.factory.createBlock(body ?? [], true),
            );
          }

          return node;
        }),
      ),
    ]).transformed;

    return createSourceFile(
      [
        ...this.#dependencyManager.imports.toCode(),
        ...file.statements.filter((statement) => !ts.isImportDeclaration(statement)),
      ],
      file.fileName,
    );
  }

  #processNumberSignalImport(path: string) {
    const { imports } = this.#dependencyManager;

    const result = imports.default.find((p) => p.includes(path));

    if (result) {
      const [path, _, id] = result;
      imports.default.remove(path);
      imports.named.add(HILLA_REACT_SIGNALS, id.text, true, id);
    }
  }
}
