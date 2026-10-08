import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function arrayNode(node: Array<unknown> | undefined, init?: EndpointRequestInit): Promise<Array<unknown> | undefined> { return client.call("JsonNodeEndpoint", "arrayNode", { node }, init); }
async function jsonNode(node: unknown, init?: EndpointRequestInit): Promise<unknown> { return client.call("JsonNodeEndpoint", "jsonNode", { node }, init); }
async function objectNode(node: unknown, init?: EndpointRequestInit): Promise<unknown> { return client.call("JsonNodeEndpoint", "objectNode", { node }, init); }
export { arrayNode, jsonNode, objectNode };
