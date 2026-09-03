import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getDefaultWildcard_1(init?: EndpointRequestInit_1): Promise<Record<string, unknown> | undefined> { return client_1.call("WildcardTypeEndpoint", "getDefaultWildcard", {}, init); }
async function getExtendingWildcard_1(init?: EndpointRequestInit_1): Promise<Array<Record<string, unknown> | undefined> | undefined> { return client_1.call("WildcardTypeEndpoint", "getExtendingWildcard", {}, init); }
async function getSuperWildcard_1(init?: EndpointRequestInit_1): Promise<Array<unknown> | undefined> { return client_1.call("WildcardTypeEndpoint", "getSuperWildcard", {}, init); }
export { getDefaultWildcard_1 as getDefaultWildcard, getExtendingWildcard_1 as getExtendingWildcard, getSuperWildcard_1 as getSuperWildcard };
