import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Child_1 from "./com/vaadin/hilla/parser/plugins/backbone/emptysuperclass/EmptySuperclassEndpoint/Child.js";
import client_1 from "./connect-client.default.js";
async function getChild_1(init?: EndpointRequestInit_1): Promise<Child_1 | undefined> { return client_1.call("EmptySuperclassEndpoint", "getChild", {}, init); }
export { getChild_1 as getChild };
