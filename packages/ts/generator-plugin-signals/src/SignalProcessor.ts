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
const INITIAL_VALUE = '$INITIAL_VALUE$';

const signals = ['NumberSignal', 'ValueSignal', 'ListSignal'];
const genericSignals = ['ValueSignal', 'ListSignal'];
const collectionSignals = ['ListSignal'];

const primitiveModels = ['StringModel', 'NumberModel', 'BooleanModel', 'ArrayModel'];

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
          const signalId = this.#replaceSignalImport(tsNode);
          let initialValue: ts.CallExpression | ts.Expression | ts.Identifier = signalId.text.startsWith('NumberSignal')
            ? ts.factory.createNumericLiteral('0')
            : ts.factory.createIdentifier('undefined');
          const filteredParams = tsNode.parameters.filter(
            (p) => !p.type || !ts.isTypeReferenceNode(p.type) || p.type.typeName !== initTypeId,
          );
          // `filteredParams` can be altered after, need to store the param names now
          const paramNames = filteredParams.map((p) => (p.name as ts.Identifier).text).join(', ');
          const isCollectionSignal = collectionSignals.includes(signalId.text);
          let genericReturnType;
          if (genericSignals.includes(signalId.text)) {
            genericReturnType = (tsNode.type as ts.TypeReferenceNode).typeArguments![0];
            if (!isCollectionSignal) {
              const defaultValueType = SignalProcessor.#getDefaultValueType(genericReturnType);
              if (defaultValueType) {
                const { modelName, createEmptyValueExpression, param } =
                  SignalProcessor.#createDefaultValueParameter(defaultValueType);
                initialValue = ts.factory.createBinaryExpression(
                  ts.factory.createPropertyAccessChain(
                    ts.factory.createIdentifier('options'),
                    ts.factory.createToken(ts.SyntaxKind.QuestionDotToken),
                    ts.factory.createIdentifier('defaultValue'),
                  ),
                  ts.factory.createToken(ts.SyntaxKind.QuestionQuestionToken),
                  createEmptyValueExpression,
                );
                filteredParams.push(param);
              }
            }
          }
          const returnType = genericReturnType ?? signalId;
          if (filteredParams.length > 0) {
            functionParams.set(tsNode.name.text, filteredParams);
          }
          return template(
            `function ${METHOD_NAME}(): ${RETURN_TYPE} {
  return new ${SIGNAL}(${isCollectionSignal ? '' : `${INITIAL_VALUE}, `}{ client: ${CONNECT_CLIENT}, endpoint: '${this.#service}', method: '${tsNode.name.text}'${paramNames.length ? `, params: { ${paramNames} }` : ''} });
}`,
            (statements) => statements,
            [
              transform((node) => (ts.isIdentifier(node) && node.text === METHOD_NAME ? tsNode.name : node)),
              transform((node) => (ts.isIdentifier(node) && node.text === SIGNAL ? signalId : node)),
              transform((node) => (ts.isIdentifier(node) && node.text === RETURN_TYPE ? returnType : node)),
              transform((node) => (ts.isIdentifier(node) && node.text === CONNECT_CLIENT ? connectClientId : node)),
              transform((node) => (ts.isIdentifier(node) && node.text === INITIAL_VALUE ? initialValue : node)),
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

  static #getDefaultValueType(node: ts.Node) {
    if (
      ts.isUnionTypeNode(node) &&
      node.types.length &&
      ts.isTypeReferenceNode(node.types[0]) &&
      node.types[0].typeArguments?.length === 1 &&
      ts.isUnionTypeNode(node.types[0].typeArguments[0])
    ) {
      return node.types[0].typeArguments[0];
    }

    return undefined;
  }

  static #createDefaultValueParameter(defaultValueType: ts.TypeNode) {
    const paramType = ts.factory.createTypeLiteralNode([
      ts.factory.createPropertySignature(
        undefined,
        ts.factory.createIdentifier('defaultValue'),
        undefined,
        defaultValueType,
      ),
    ]);

    let undefinedDefaultValue: ts.Identifier | undefined;

    if (
      ts.isUnionTypeNode(defaultValueType) &&
      defaultValueType.types.length &&
      defaultValueType.types.length > 1 &&
      defaultValueType.types.map((t) => t.kind).includes(ts.SyntaxKind.UndefinedKeyword)
    ) {
      undefinedDefaultValue = ts.factory.createIdentifier('undefined');
    }

    let emptyValueGeneratorResult: {
      modelName: string | undefined;
      callExpression: ts.CallExpression | ts.Identifier;
    } = {
      modelName: undefined,
      callExpression: ts.factory.createIdentifier('undefined'),
    };

    if (undefinedDefaultValue === undefined) {
      emptyValueGeneratorResult = SignalProcessor.#createEmptyValueGeneratorExpression(defaultValueType);
    }
    // Return the model name that is needed for the imports along with the parameter and empty value creation expression
    return {
      modelName: emptyValueGeneratorResult.modelName,
      createEmptyValueExpression: emptyValueGeneratorResult.callExpression,
      param: ts.factory.createParameterDeclaration(
        undefined,
        undefined,
        'options',
        ts.factory.createToken(ts.SyntaxKind.QuestionToken),
        paramType,
      ),
    };
  }

  static #createEmptyValueGeneratorExpression(returnType: ts.TypeNode) {
    let modelName = '';
    const typeNodeObject = returnType as ts.UnionTypeNode;
    switch (typeNodeObject.types[0].kind) {
      case ts.SyntaxKind.StringKeyword:
        modelName = 'StringModel';
        break;
      case ts.SyntaxKind.NumberKeyword:
        modelName = 'NumberModel';
        break;
      case ts.SyntaxKind.BooleanKeyword:
        modelName = 'BooleanModel';
        break;
      case ts.SyntaxKind.ArrayType: // for [] but doesn't work for Array<T>
        modelName = 'ArrayModel';
        break;
      default:
        modelName = 'ObjectModel';
    }
    const callExpression = ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(ts.factory.createIdentifier(modelName), 'createEmptyValue'),
      undefined,
      [],
    );
    return {
      modelName,
      callExpression,
    };
  }

  /*
  static #createDefaultValueParameter(returnType: ts.TypeNode) {
    const alias = createFullyUniqueIdentifier('defaultValue');
    const bindingPattern = ts.factory.createObjectBindingPattern([
      ts.factory.createBindingElement(undefined, ts.factory.createIdentifier('defaultValue'), alias, undefined),
    ]);
    const paramType = ts.factory.createTypeLiteralNode([
      ts.factory.createPropertySignature(undefined, ts.factory.createIdentifier('defaultValue'), undefined, returnType),
    ]);
    // Return both the alias and the full parameter
    return {
      alias,
      param: ts.factory.createParameterDeclaration(undefined, undefined, bindingPattern, undefined, paramType),
    };
  }*/

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
