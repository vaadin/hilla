import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type GenericsBareRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
import type GenericsExtendedRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsExtendedRefEntity.js";
import client_1 from "./connect-client.default.js";
async function getBareReference_1(ref: GenericsBareRefEntity_1<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<string | undefined> | undefined> { return client_1.call("GenericsRefEndpoint", "getBareReference", { ref }, init); }
async function getExtendedReference_1(ref: GenericsExtendedRefEntity_1<GenericsBareRefEntity_1<string | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsExtendedRefEntity_1<GenericsBareRefEntity_1<string | undefined> | undefined> | undefined> { return client_1.call("GenericsRefEndpoint", "getExtendedReference", { ref }, init); }
export { getBareReference_1 as getBareReference, getExtendedReference_1 as getExtendedReference };

