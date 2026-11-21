import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type ConfiguredKindBase from "./com/vaadin/hilla/parser/plugins/backbone/config/CustomConfigEndpoint/ConfiguredKindBase.js";
import type CustomConfigEntity from "./com/vaadin/hilla/parser/plugins/backbone/config/CustomConfigEndpoint/CustomConfigEntity.js";
import client from "./connect-client.default.js";
async function get(init?: EndpointRequestInit): Promise<CustomConfigEntity | undefined> { return client.call("CustomConfigEndpoint", "get", {}, init); }
async function getConfiguredKind(init?: EndpointRequestInit): Promise<ConfiguredKindBase | undefined> { return client.call("CustomConfigEndpoint", "getConfiguredKind", {}, init); }
export { get, getConfiguredKind };
