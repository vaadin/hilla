import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type GenericsBareEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareEntityEndpoint/GenericsBareEntity.js";
import type GenericsRecord_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareEntityEndpoint/GenericsRecord.js";
import type GenericsBareRefEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
import client_1 from "./connect-client.default.js";
async function getBareEntity_1(ref: GenericsBareRefEntity_1<GenericsBareEntity_1 | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<GenericsBareEntity_1 | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getBareEntity", { ref }, init); }
async function getBareEntityList_1(ref: GenericsBareRefEntity_1<Array<number | undefined> | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<Array<number | undefined> | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getBareEntityList", { ref }, init); }
async function getBareReference_1(ref: GenericsBareRefEntity_1<string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsBareRefEntity_1<string | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getBareReference", { ref }, init); }
async function getRecord_1(record: GenericsRecord_1<string | undefined, string | undefined> | undefined, init?: EndpointRequestInit_1): Promise<GenericsRecord_1<string | undefined, string | undefined> | undefined> { return client_1.call("GenericsBareEntityEndpoint", "getRecord", { record }, init); }
export { getBareEntity_1 as getBareEntity, getBareEntityList_1 as getBareEntityList, getBareReference_1 as getBareReference, getRecord_1 as getRecord };

