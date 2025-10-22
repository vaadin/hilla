import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type ExposedInterfaceEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedInterfaceEntity.js";
import type ExposedSuperclassEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedSuperclassEntity.js";
import client_1 from "./connect-client.default.js";
async function methodFromExposedSuperclass_1(init?: EndpointRequestInit_1): Promise<ExposedSuperclassEntity_1 | undefined> { return client_1.call("ExposedEndpoint", "methodFromExposedSuperclass", {}, init); }
async function methodFromExposedInterface_1(init?: EndpointRequestInit_1): Promise<ExposedInterfaceEntity_1 | undefined> { return client_1.call("ExposedEndpoint", "methodFromExposedInterface", {}, init); }
async function methodFromEndpoint_1(init?: EndpointRequestInit_1): Promise<void> { return client_1.call("ExposedEndpoint", "methodFromEndpoint", {}, init); }
export { methodFromEndpoint_1 as methodFromEndpoint, methodFromExposedInterface_1 as methodFromExposedInterface, methodFromExposedSuperclass_1 as methodFromExposedSuperclass };
