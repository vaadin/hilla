import { basename, dirname, extname } from 'node:path';
import {
  factory,
  isTypeReferenceNode,
  isIdentifier,
  type Identifier,
  type ReturnStatement,
  type SourceFile,
  type TypeNode,
  type ParameterDeclaration,
  type BinaryExpression,
  type Node,
  SyntaxKind,
  isTypeNode,
  transform,
  isFunctionDeclaration,
  isImportDeclaration,
  isUnionTypeNode,
  type PropertyAccessExpression,
} from '@typescript/typescript6';
import type Plugin from '@vaadin/hilla-generator-core/Plugin.js';
import { simplifyFullyQualifiedName } from '@vaadin/hilla-generator-core/Schema.js';
import { traverse } from '@vaadin/hilla-generator-utils/ast.js';
import createSourceFile from '@vaadin/hilla-generator-utils/createSourceFile.js';
import DependencyManager from '@vaadin/hilla-generator-utils/dependencies/DependencyManager.js';
import PathManager from '@vaadin/hilla-generator-utils/dependencies/PathManager.js';
import ast, { createTransformer } from '@vaadin/hilla-generator-utils/tsc-template.js';
import { ARRAY_TYPES, COLLECTION_SIGNALS, GENERIC_SIGNALS } from './utils.js';

const HILLA_REACT_SIGNALS = '@vaadin/hilla-react-signals';
const HILLA_LIT_FORM = '@vaadin/hilla-lit-form';

export default class SignalProcessor {
  readonly #dependencyManager: DependencyManager;
  readonly #owner: Plugin;
  readonly #service: string;
  readonly #methods: Map<string, string>;
  readonly #methodsByDeclaredName: ReadonlyMap<string, string>;
  readonly #sourceFile: SourceFile;

  constructor(service: string, methods: Map<string, string>, sourceFile: SourceFile, owner: Plugin) {
    this.#service = service;
    this.#methods = methods;
    this.#sourceFile = sourceFile;
    this.#owner = owner;
    this.#dependencyManager = new DependencyManager(new PathManager({ extension: '.js' }));
    this.#dependencyManager.imports.fromCode(this.#sourceFile);
    this.#dependencyManager.exports.fromCode(this.#sourceFile);

    const { exports } = this.#dependencyManager;
    // a method whose name is a reserved word is declared under a suffixed one
    this.#methodsByDeclaredName = new Map(
      Iterator.from(methods.keys()).map((method) => [exports.named.getIdentifier(method)?.text ?? method, method]),
    );
  }

  process(): SourceFile {
    this.#owner.logger.debug(`Processing signals: ${this.#service}`);
    const { imports } = this.#dependencyManager;

    const [, connectClientId] = Iterator.from(imports.default).find(([path]) => path.includes('connect-client'))!;

    const initTypeId = imports.named.getIdentifier('@vaadin/hilla-frontend', 'EndpointRequestInit');
    let initTypeUsageCount = 0;

    const [file] = transform(this.#sourceFile, [
      createTransformer((node) => {
        if (isFunctionDeclaration(node)) {
          // The declared name is not the method name when the latter is a
          // reserved word.
          const methodName = node.name && this.#methodsByDeclaredName.get(node.name.text);

          // Check if the function is a signal method.
          if (methodName) {
            // Get or add the signal class identifier for runtime use.
            const signalClassName = simplifyFullyQualifiedName(this.#methods.get(methodName)!);
            const signalId =
              imports.named.getIdentifier(HILLA_REACT_SIGNALS, signalClassName) ??
              imports.named.add(HILLA_REACT_SIGNALS, signalClassName);

            // Remove the `init` parameter.
            const params = node.parameters.filter(
              (p) => !(p.type && isTypeReferenceNode(p.type) && p.type.typeName === initTypeId),
            );

            // Calculate the default value for the signal class.
            const { defaultValue, defaultValueParameter } = this.#createDefaultValue(
              signalClassName,
              signalId,
              node.type,
            );

            // Remove the `async` modifier if present.
            const modifiers = node.modifiers?.filter((m) => m.kind !== SyntaxKind.AsyncKeyword);

            // Remove the `Promise` type if present.
            const type =
              node.type &&
              isTypeReferenceNode(node.type) &&
              isIdentifier(node.type.typeName) &&
              node.type.typeName.text === 'Promise'
                ? node.type.typeArguments?.[0]
                : node.type;

            // Get the variable parameter names to be passed to the signal
            // class.
            const paramNames = params
              .map((p) => p.name)
              .filter((n) => isIdentifier(n))
              .map((n) => n.text);

            // Create the signal class method body applying all the data we've
            // gathered.
            const result = ast`function dummy() { %{
              return new ${signalId}(${defaultValue}${defaultValue ? ',' : ''}{
                client: ${connectClientId},
                endpoint: '${this.#service}',
                method: '${methodName}'
                ${paramNames.length ? `, params: { ${paramNames.join('\n')} }` : ''} });
            }% });`;

            // Update the function declaration accordingly.
            return factory.updateFunctionDeclaration(
              node,
              modifiers,
              node.asteriskToken,
              node.name,
              node.typeParameters,
              defaultValueParameter ? [...params, defaultValueParameter] : params,
              type,
              factory.createBlock([result.node as ReturnStatement]),
            );
          } else if (
            node.parameters.some((p) => p.type && isTypeReferenceNode(p.type) && p.type.typeName === initTypeId)
          ) {
            // Count the number of times the `init` parameter is used to check
            // if the type import is necessary to be removed.
            initTypeUsageCount += 1;
          }
        }

        return node;
      }),
    ]).transformed;

    // Remove the `EndpointRequestInit` import if it is not used anymore.
    if (initTypeUsageCount === 0) {
      imports.named.remove('@vaadin/hilla-frontend', 'EndpointRequestInit');
    }

    return createSourceFile(
      [
        ...this.#dependencyManager.imports.toCode(),
        ...file.statements.filter((statement) => !isImportDeclaration(statement)),
      ],
      file.fileName,
    );
  }

  // The class name is the one of the signal itself, while the identifier is how
  // the file refers to it, which carries a suffix on a collision.
  #createDefaultValue(signalClassName: string, signalId: Identifier, returnType?: TypeNode) {
    const { imports } = this.#dependencyManager;

    // If the signal class is a collection signal, we have no default value to
    // generate.
    if (COLLECTION_SIGNALS.includes(signalClassName)) {
      return {};
    }

    // If we have the NumberSignal class, we can use `0` as the default.
    if (!GENERIC_SIGNALS.includes(signalClassName)) {
      return signalClassName.startsWith('NumberSignal') ? { defaultValue: '0' } : {};
    }

    // Extract the generic argument of the signal class to get the default
    // value type.
    const type = traverse(returnType!, (node) =>
      isTypeReferenceNode(node) &&
      isIdentifier(node.typeName) &&
      node.typeName.text === signalId.text &&
      node.typeArguments
        ? node.typeArguments[0]
        : undefined,
    )!;

    // Import the model class for the signal type (or omitting the import if
    // the type is a nullable one).
    const modelId = traverse(type, (node) => {
      // In case the generic argument of a signal class is nullable (e.g.
      // `Signal<Person | undefined>`), the default value will be `undefined`.
      if (isUnionTypeNode(node) && node.types.length > 1 && node.types[1].kind === SyntaxKind.UndefinedKeyword) {
        return SyntaxKind.UndefinedKeyword;
      }

      // Otherwise, we have to import the model class.
      return this.#getModelId(node);
    })!;

    // If we have a signal class with a generic argument (but not a collection),
    // we need to provide a possibility to set the default value.
    const optionsMethodTypeId =
      imports.named.getIdentifier(HILLA_REACT_SIGNALS, 'SignalMethodOptions') ??
      imports.named.add(HILLA_REACT_SIGNALS, 'SignalMethodOptions');

    return {
      defaultValue:
        modelId === SyntaxKind.UndefinedKeyword
          ? (ast`options?.defaultValue`.node as PropertyAccessExpression)
          : (ast`options?.defaultValue ?? ${modelId}.createEmptyValue()`.node as BinaryExpression),
      defaultValueParameter: ast`function dummy( %{ options?: ${optionsMethodTypeId}<${type}> }% ) {}`
        .node as ParameterDeclaration,
    };
  }

  #getModelId(node: Node) {
    const { imports } = this.#dependencyManager;

    if (isIdentifier(node)) {
      // In case the node is an array type defined as `Array<T>` or
      // `ReadonlyArray<T>`, we need to import the `ArrayModel` class.
      if (ARRAY_TYPES.includes(node.text)) {
        return (
          imports.named.getIdentifier(HILLA_LIT_FORM, 'ArrayModel') ?? imports.named.add(HILLA_LIT_FORM, 'ArrayModel')
        );
      }

      // Otherwise, we calculate and import the model class.
      const [path] = Iterator.from(imports.default).find(([, id]) => id === node) ?? [];

      if (!path) {
        throw new Error(`Model not found for ${node.text}`);
      }

      // the path is what the model name comes from: the identifier may carry a
      // suffix, while the file the entity lives in never does
      const modelName = `${basename(path, extname(path))}Model`;
      const modelPath = `${dirname(path)}/${modelName}${extname(path)}`;

      return imports.default.getIdentifier(modelPath) ?? imports.default.add(modelPath, modelName);
    } else if (isTypeNode(node)) {
      // If the node is a primitive type, we will import the corresponding
      // model class from `@vaadin/hilla-lit-form`.
      let modelName: string | undefined;

      switch (node.kind) {
        case SyntaxKind.StringKeyword:
          modelName = 'StringModel';
          break;
        case SyntaxKind.NumberKeyword:
          modelName = 'NumberModel';
          break;
        case SyntaxKind.BooleanKeyword:
          modelName = 'BooleanModel';
          break;
        case SyntaxKind.ArrayType:
          modelName = 'ArrayModel';
          break;
        default:
          return undefined;
      }

      return imports.named.getIdentifier(HILLA_LIT_FORM, modelName) ?? imports.named.add(HILLA_LIT_FORM, modelName);
    }

    return undefined;
  }
}
