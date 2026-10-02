import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getDefaultWildcard(init?: EndpointRequestInit): Promise<Record<string, unknown> | undefined> { return client.call("WildcardTypeEndpoint", "getDefaultWildcard", {}, init); }
async function getExtendingWildcard(init?: EndpointRequestInit): Promise<Array<Record<string, unknown> | undefined> | undefined> { return client.call("WildcardTypeEndpoint", "getExtendingWildcard", {}, init); }
async function getSuperWildcard(init?: EndpointRequestInit): Promise<Array<unknown> | undefined> { return client.call("WildcardTypeEndpoint", "getSuperWildcard", {}, init); }
export { getDefaultWildcard, getExtendingWildcard, getSuperWildcard };
