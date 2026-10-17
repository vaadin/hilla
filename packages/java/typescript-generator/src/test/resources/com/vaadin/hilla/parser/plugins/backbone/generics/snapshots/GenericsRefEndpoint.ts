import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type GenericsBareRefEntity from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
import type GenericsExtendedRefEntity from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsExtendedRefEntity.js";
import client from "./connect-client.default.js";
async function getBareReference(ref: GenericsBareRefEntity<string | undefined> | undefined, init?: EndpointRequestInit): Promise<GenericsBareRefEntity<string | undefined> | undefined> { return client.call("GenericsRefEndpoint", "getBareReference", { ref }, init); }
async function getExtendedReference(ref: GenericsExtendedRefEntity<GenericsBareRefEntity<string | undefined> | undefined> | undefined, init?: EndpointRequestInit): Promise<GenericsExtendedRefEntity<GenericsBareRefEntity<string | undefined> | undefined> | undefined> { return client.call("GenericsRefEndpoint", "getExtendedReference", { ref }, init); }
export { getBareReference, getExtendedReference };
