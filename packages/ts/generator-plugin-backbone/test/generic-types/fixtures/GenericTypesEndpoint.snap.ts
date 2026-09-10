import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type GenericTypesEntity from "./com/vaadin/hilla/parser/plugins/backbone/generictypes/GenericTypesEntity.js";
import client from "./connect-client.default.js";
async function unwrap(value: GenericTypesEntity<string | undefined>, init?: EndpointRequestInit): Promise<string> { return client.call("GenericTypesEndpoint", "unwrap", { value }, init); }
async function wrap(value: string, init?: EndpointRequestInit): Promise<GenericTypesEntity<string>> { return client.call("GenericTypesEndpoint", "wrap", { value }, init); }
export { unwrap, wrap };
