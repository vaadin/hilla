import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type ConcreteType_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/ConcreteType.js";
import client_1 from "./connect-client.default.js";
async function dealWithConcreteType_1(object: ConcreteType_1 | undefined, init?: EndpointRequestInit_1): Promise<ConcreteType_1 | undefined> { return client_1.call("ImplementInterfaceEndpoint", "dealWithConcreteType", { object }, init); }
async function dealWithGenericType_1(object: ConcreteType_1 | undefined, init?: EndpointRequestInit_1): Promise<ConcreteType_1 | undefined> { return client_1.call("ImplementInterfaceEndpoint", "dealWithGenericType", { object }, init); }
async function dealWithItAgain_1(object: ConcreteType_1 | undefined, init?: EndpointRequestInit_1): Promise<ConcreteType_1 | undefined> { return client_1.call("ImplementInterfaceEndpoint", "dealWithItAgain", { object }, init); }
export { dealWithConcreteType_1 as dealWithConcreteType, dealWithGenericType_1 as dealWithGenericType, dealWithItAgain_1 as dealWithItAgain };

