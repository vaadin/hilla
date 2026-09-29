import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Entity_1 from "./com/vaadin/hilla/parser/plugins/backbone/importclash/Entity.js";
import type Record_1 from "./com/vaadin/hilla/parser/plugins/backbone/importclash/Record.js";
import client_1 from "./connect-client.default.js";
async function getClientParam(client: string, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("ImportClashEndpoint", "getClientParam", { client }, init); }
async function getInitTypeParam(EndpointRequestInit: string, init?: EndpointRequestInit_1): Promise<string> { return client_1.call("ImportClashEndpoint", "getInitTypeParam", { EndpointRequestInit }, init); }
async function getEntityParam(Entity: Entity_1, init?: EndpointRequestInit_1): Promise<Entity_1> { return client_1.call("ImportClashEndpoint", "getEntityParam", { Entity }, init); }
async function client(init?: EndpointRequestInit_1): Promise<string> { return client_1.call("ImportClashEndpoint", "client", {}, init); }
async function getRecordMap(Record: Record_1, init?: EndpointRequestInit_1): Promise<Record<string, Record_1> | undefined> { return client_1.call("ImportClashEndpoint", "getRecordMap", { Record }, init); }
export { client, getClientParam, getEntityParam, getInitTypeParam, getRecordMap };
