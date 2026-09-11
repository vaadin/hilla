import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type GenericsBareEntity from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareEntityEndpoint/GenericsBareEntity.js";
import type GenericsRecord from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareEntityEndpoint/GenericsRecord.js";
import type GenericsBareRefEntity from "./com/vaadin/hilla/parser/plugins/backbone/generics/GenericsBareRefEntity.js";
import client from "./connect-client.default.js";
async function getBareEntity(ref: GenericsBareRefEntity<GenericsBareEntity | undefined> | undefined, init?: EndpointRequestInit): Promise<GenericsBareRefEntity<GenericsBareEntity | undefined> | undefined> { return client.call("GenericsBareEntityEndpoint", "getBareEntity", { ref }, init); }
async function getBareEntityList(ref: GenericsBareRefEntity<Array<number | undefined> | undefined> | undefined, init?: EndpointRequestInit): Promise<GenericsBareRefEntity<Array<number | undefined> | undefined> | undefined> { return client.call("GenericsBareEntityEndpoint", "getBareEntityList", { ref }, init); }
async function getBareReference(ref: GenericsBareRefEntity<string | undefined> | undefined, init?: EndpointRequestInit): Promise<GenericsBareRefEntity<string | undefined> | undefined> { return client.call("GenericsBareEntityEndpoint", "getBareReference", { ref }, init); }
async function getRecord(record: GenericsRecord<string | undefined, string | undefined> | undefined, init?: EndpointRequestInit): Promise<GenericsRecord<string | undefined, string | undefined> | undefined> { return client.call("GenericsBareEntityEndpoint", "getRecord", { record }, init); }
export { getBareEntity, getBareEntityList, getBareReference, getRecord };
