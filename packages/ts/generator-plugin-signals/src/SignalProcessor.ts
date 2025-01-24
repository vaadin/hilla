import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { template, transform, traverse } from '@vaadin/hilla-generator-utils/ast.js';
import createFullyUniqueIdentifier from '@vaadin/hilla-generator-utils/createFullyUniqueIdentifier.js';
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

const primitiveModels = Object.freeze(
  new Map<ts.SyntaxKind, string>([
    [ts.SyntaxKind.StringKeyword, 'StringModel'],
    [ts.SyntaxKind.NumberKeyword, 'NumberModel'],
    [ts.SyntaxKind.BooleanKeyword, 'BooleanModel'],
    [ts.SyntaxKind.ArrayType, 'ArrayModel'],
  ]),
);

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
          const filteredParams = tsNode.parameters.filter(
            (p) => !p.type || !ts.isTypeReferenceNode(p.type) || p.type.typeName !== initTypeId,
          );
          // `filteredParams` can be altered after, need to store the param names now
          const paramNames = filteredParams.map((p) => (p.name as ts.Identifier).text).join(', ');
          const isCollectionSignal = collectionSignals.includes(signalId.text);

          const { defaultValueExpression, defaultValueParam, genericReturnType } = this.#createDefaultValue(
            signalId,
            tsNode,
          );
          if (defaultValueParam) {
            filteredParams.push(defaultValueParam);
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
              transform((node) =>
                ts.isIdentifier(node) && node.text === INITIAL_VALUE ? defaultValueExpression : node,
              ),
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

  #createDefaultValue(signalId: ts.Identifier, functionDeclaration: FunctionDeclaration) {
    let defaultValueExpression: ts.Expression | ts.Identifier = signalId.text.startsWith('NumberSignal')
      ? ts.factory.createNumericLiteral('0')
      : ts.factory.createIdentifier('undefined');
    let defaultValueParam: ts.ParameterDeclaration | undefined;
    let genericReturnType;
    if (genericSignals.includes(signalId.text)) {
      genericReturnType = (functionDeclaration.type as ts.TypeReferenceNode).typeArguments![0];
      if (!collectionSignals.includes(signalId.text)) {
        const defaultValueType = SignalProcessor.#getDefaultValueType(genericReturnType);
        if (defaultValueType) {
          defaultValueParam = SignalProcessor.#createDefaultValueParameter(defaultValueType);

          const emptyValueExpression = this.#createEmptyValueExpression(defaultValueType);
          defaultValueExpression = ts.factory.createBinaryExpression(
            ts.factory.createPropertyAccessChain(
              ts.factory.createIdentifier('options'),
              ts.factory.createToken(ts.SyntaxKind.QuestionDotToken),
              ts.factory.createIdentifier('defaultValue'),
            ),
            ts.factory.createToken(ts.SyntaxKind.QuestionQuestionToken),
            emptyValueExpression,
          );
        }
      }
    }
    return { defaultValueExpression, defaultValueParam, genericReturnType };
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

    return ts.factory.createParameterDeclaration(
      undefined,
      undefined,
      'options',
      ts.factory.createToken(ts.SyntaxKind.QuestionToken),
      paramType,
    );
  }

  static #isDefaultValueTypeNullable(defaultValueType: ts.TypeNode) {
    return (
      ts.isUnionTypeNode(defaultValueType) &&
      defaultValueType.types.length &&
      defaultValueType.types.length > 1 &&
      defaultValueType.types.map((t) => t.kind).includes(ts.SyntaxKind.UndefinedKeyword)
    );
  }

  #createEmptyValueExpression(defaultValueType: ts.UnionTypeNode) {
    if (SignalProcessor.#isDefaultValueTypeNullable(defaultValueType)) {
      return ts.factory.createIdentifier('undefined');
    }
    const importedModelUniqueId = this.#determineModelImportUniqueIdentifier(defaultValueType);
    return ts.factory.createCallExpression(
      ts.factory.createPropertyAccessExpression(importedModelUniqueId, 'createEmptyValue'),
      undefined,
      [],
    );
  }

  #determineModelImportUniqueIdentifier(returnTypeNode: ts.UnionTypeNode) {
    let modelName = primitiveModels.get(returnTypeNode.types[0].kind);
    let entityName;
    if (modelName === undefined) {
      const { entityName: e, modelName: m } = SignalProcessor.#extractModelNameFromTypeNode(returnTypeNode);
      modelName = m;
      entityName = e;
    }
    const modelImportUniqueId =
      this.#getExistingEntityModelUniqueIdentifier(modelName) ?? createFullyUniqueIdentifier(modelName);

    this.#addModelImport(entityName, modelName, modelImportUniqueId);
    return modelImportUniqueId;
  }

  static #extractModelNameFromTypeNode(returnTypeNode: ts.UnionTypeNode) {
    if (ts.isTypeReferenceNode(returnTypeNode.types[0])) {
      const typeIdentifier = returnTypeNode.types[0].typeName;
      if (ts.isIdentifier(typeIdentifier)) {
        const entityName = typeIdentifier.text;
        const modelName = `${entityName}Model`;
        return { entityName, modelName };
      }
    }
    throw new Error('Unsupported type reference node');
  }

  #getExistingEntityModelUniqueIdentifier(modelName: string) {
    const { imports } = this.#dependencyManager;
    return (
      imports.named.getIdentifier('@vaadin/hilla-lit-form', modelName) ??
      imports.default.iter().find(([path]) => path.endsWith(`/${modelName}.js`))?.[1]
    );
  }

  #addModelImport(
    entityName: string | undefined,
    modelName: string | undefined,
    modelNameUniqueId: ts.Identifier | undefined,
  ) {
    if (modelName === undefined) {
      return;
    }
    if (primitiveModels.values().find((primitiveModel) => primitiveModel === modelName)) {
      const { imports } = this.#dependencyManager;
      const importedModel = imports.named.getIdentifier('@vaadin/hilla-lit-form', modelName);
      if (importedModel === undefined) {
        imports.named.add('@vaadin/hilla-lit-form', modelName, false, modelNameUniqueId);
      }
    } else {
      this.#addObjectModelImport(entityName!, modelName, modelNameUniqueId!);
    }
  }

  #addObjectModelImport(entityName: string, modelName: string, modelNameUniqueId: ts.Identifier) {
    const { imports } = this.#dependencyManager;
    const entityImport = imports.default
      .iter()
      .map(([path]) => path)
      .find((path) => path.startsWith('./') && path.endsWith(`/${entityName}.js`));
    if (entityImport === undefined) {
      throw new Error(`Import for Entity '${entityName}' not found`);
    }
    const entityModelImportPath = entityImport.replace(`/${entityName}.js`, `/${modelName}.js`);
    const importedModel = imports.default.paths().find((path) => path === entityModelImportPath);
    if (importedModel === undefined) {
      imports.default.add(entityModelImportPath, modelName, false, modelNameUniqueId);
    }
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
