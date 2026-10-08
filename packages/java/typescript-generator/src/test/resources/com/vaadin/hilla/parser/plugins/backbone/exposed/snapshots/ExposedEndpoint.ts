import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type ExposedInterfaceEntity from "./com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedInterfaceEntity.js";
import type ExposedSuperclassEntity from "./com/vaadin/hilla/parser/plugins/backbone/exposed/ExposedSuperclassEntity.js";
import client from "./connect-client.default.js";
async function methodFromExposedSuperclass(init?: EndpointRequestInit): Promise<ExposedSuperclassEntity | undefined> { return client.call("ExposedEndpoint", "methodFromExposedSuperclass", {}, init); }
async function methodFromExposedInterface(init?: EndpointRequestInit): Promise<ExposedInterfaceEntity | undefined> { return client.call("ExposedEndpoint", "methodFromExposedInterface", {}, init); }
async function methodFromEndpoint(init?: EndpointRequestInit): Promise<void> { return client.call("ExposedEndpoint", "methodFromEndpoint", {}, init); }
export { methodFromEndpoint, methodFromExposedInterface, methodFromExposedSuperclass };
