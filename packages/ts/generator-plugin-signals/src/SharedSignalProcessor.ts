import type SharedStorage from "@vaadin/hilla-generator-core/SharedStorage.js";
import type {PathSignalType} from "./index";
import ts, {type CallExpression, type Identifier, type Node} from "typescript";
import { template, transform } from "@vaadin/hilla-generator-utils/ast.js";

type MethodInfo = {
  name: string;
  signalType: string;
}

type ServiceInfo = {
  service: string;
  methods: MethodInfo[];
}

const FUNCTION_NAME = '$FUNCTION_NAME$';
const RETURN_TYPE = '$RETURN_TYPE$';
const ENDPOINT_CALL_EXPRESSION = '$ENDPOINT_CALL_EXPRESSION$';

const groupByService = (signals: PathSignalType[]): Map<string, MethodInfo[] | undefined> => {
  const serviceMap = new Map<string, MethodInfo[] | undefined>();

  signals.forEach(signal => {
    const [_, service, method] = signal.path.split('/');

    const serviceMethods = serviceMap.get(service) ?? [] ;

    serviceMethods.push({
      name: method,
      signalType: signal.signalType,
    });

    serviceMap.set(service, serviceMethods);
  });

  return serviceMap;
};

function extractEndpointCallExpression(method: MethodInfo, serviceSource: ts.SourceFile): ts.CallExpression | undefined {
  const fn = serviceSource.statements.filter((node) => ts.isFunctionDeclaration(node) && node.name?.text === method.name)[0];
  let callExpression: CallExpression | undefined;
  ts.transform(fn as Node, [transform((node) => {
    if (ts.isCallExpression(node) && ts.isPropertyAccessExpression(node.expression) && ts.isIdentifier(node.expression.name) && node.expression.name.text === 'call') {
      callExpression = node;
    }
    return node;
  })]);
  return callExpression;
}

function transformMethod(method: MethodInfo, sourceFile: ts.SourceFile): void {
  const endpointCallExpression = extractEndpointCallExpression(method, sourceFile);

  const ast = template(`
  const sharedSignal = await ${ENDPOINT_CALL_EXPRESSION};
  const queueDescriptor = {
    id: sharedSignal.id,
    subscribe: SignalsHandler.subscribe,
    publish: SignalsHandler.update,
  }
  const valueLog = new NumberSignalQueue(queueDescriptor, connectClient);
  return valueLog.getRoot();
  `, (statements) => statements,
    []);
}

function processSignalService(service: string, methods: MethodInfo[], sharedStorage: SharedStorage): void {
  // Process the signal service
  const serviceSource = sharedStorage.sources.filter((source) => source.fileName === `${service}.ts`)[0];
  if (serviceSource) {
    methods.forEach((method) => transformMethod(method, serviceSource));
  }
  sharedStorage.sources.splice(sharedStorage.sources.indexOf(serviceSource), 1);
}

export default function process(pathsWithSignals: PathSignalType[], sharedStorage: SharedStorage): void {
  // group methods by service:
  const services = groupByService(pathsWithSignals);
  services.forEach((serviceInfo) => {
    processSignalService(serviceInfo.service, serviceInfo.methods, sharedStorage);
  });
}
