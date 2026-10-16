import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Child from "./com/vaadin/hilla/parser/plugins/backbone/emptysuperclass/EmptySuperclassEndpoint/Child.js";
import client from "./connect-client.default.js";
async function getChild(init?: EndpointRequestInit): Promise<Child | undefined> { return client.call("EmptySuperclassEndpoint", "getChild", {}, init); }
export { getChild };
