import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { template, transform, traverse } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ts, { type FunctionDeclaration, type Identifier, type SourceFile } from 'typescript';

const HILLA_REACT_SIGNALS = '@vaadin/hilla-react-signals';

const CONNECT_CLIENT = '$CONNECT_CLIENT$';
const METHOD_NAME = '$METHOD_NAME$';
const SIGNAL = '$SIGNAL$';
const RETURN_TYPE = '$RETURN_TYPE$';

const signals = ['NumberSignal', 'ValueSignal'];
const genericSignals = ['ValueSignal'];

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

    const [, connectClientId] = imports.default.iter().find(([path]) => path.includes('connect-client'))!;

    const initTypeId = imports.named.getIdentifier('@vaadin/hilla-frontend', 'EndpointRequestInit');
    let initTypeUsageCount = 0;
    const functionParams: Map<string, ts.ParameterDeclaration[]> = new Map<string, ts.ParameterDeclaration[]>();

    const [file] = ts.transform<SourceFile>(this.#sourceFile, [
      transform((tsNode) => {
        if (ts.isFunctionDeclaration(tsNode) && tsNode.name && this.#methods.has(tsNode.name.text)) {
          const filteredParams = tsNode.parameters.filter(
            (p) => !p.type || !ts.isTypeReferenceNode(p.type) || p.type.typeName !== initTypeId,
          );
          if (filteredParams.length > 0) {
            functionParams.set(tsNode.name.text, filteredParams);
          }
          const paramNames = filteredParams.map((p) => (p.name as ts.Identifier).text).join(', ');
          const signalId = this.#replaceSignalImport(tsNode);
          let genericReturnType;
          if (genericSignals.includes(signalId.text)) {
            genericReturnType = (tsNode.type as ts.TypeReferenceNode).typeArguments![0];
          }
          const returnType = genericReturnType ?? signalId;
          const INITIAL_VALUE = signalId.text.startsWith('NumberSignal') ? '0' : 'undefined';
          return template(
            `function ${METHOD_NAME}(): ${RETURN_TYPE} {
  return new ${SIGNAL}(${INITIAL_VALUE}, { client: ${CONNECT_CLIENT}, endpoint: '${this.#service}', method: '${tsNode.name.text}'${filteredParams.length > 0 ? `, params: { ${paramNames} }` : ''} });
}`,
            (statements) => statements,
            [
              transform((node) => (ts.isIdentifier(node) && node.text === METHOD_NAME ? tsNode.name : node)),
              transform((node) => (ts.isIdentifier(node) && node.text === SIGNAL ? signalId : node)),
              transform((node) => (ts.isIdentifier(node) && node.text === RETURN_TYPE ? returnType : node)),
              transform((node) => (ts.isIdentifier(node) && node.text === CONNECT_CLIENT ? connectClientId : node)),
            ],
          );
        }
        return tsNode;
      }),
      transform((tsNode) => {
        if (
          ts.isFunctionDeclaration(tsNode) &&
          tsNode.name &&
          this.#methods.has(tsNode.name.text) &&
          functionParams.has(tsNode.name.text)
        ) {
          return ts.factory.updateFunctionDeclaration(
            tsNode,
            tsNode.modifiers,
            tsNode.asteriskToken,
            tsNode.name,
            tsNode.typeParameters,
            functionParams.get(tsNode.name.text)!,
            tsNode.type,
            tsNode.body,
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

  #replaceSignalImport(method: FunctionDeclaration): Identifier {
    const { imports } = this.#dependencyManager;

    if (method.type) {
      const type = traverse(method.type, (node) =>
        ts.isIdentifier(node) && signals.includes(node.text) ? node : undefined,
      );

      if (type) {
        const signalId = imports.named.getIdentifier(HILLA_REACT_SIGNALS, type.text);

        if (signalId) {
          return signalId;
        }

        const result = imports.default.iter().find(([_p, id]) => id.text === type.text);

        if (result) {
          const [path] = result;
          imports.default.remove(path);
          return imports.named.add(HILLA_REACT_SIGNALS, type.text, false, type);
        }
      }
    }

    throw new Error('Signal type not found');
  }
}
