import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function arrayNode_1(node: Array<unknown> | undefined, init?: EndpointRequestInit_1): Promise<Array<unknown> | undefined> { return client_1.call("JsonNodeEndpoint", "arrayNode", { node }, init); }
async function jsonNode_1(node: unknown, init?: EndpointRequestInit_1): Promise<unknown> { return client_1.call("JsonNodeEndpoint", "jsonNode", { node }, init); }
async function objectNode_1(node: unknown, init?: EndpointRequestInit_1): Promise<unknown> { return client_1.call("JsonNodeEndpoint", "objectNode", { node }, init); }
export { arrayNode_1 as arrayNode, jsonNode_1 as jsonNode, objectNode_1 as objectNode };
