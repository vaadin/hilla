import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type DeclaredBase_1 from "./com/vaadin/hilla/parser/plugins/subtypes/existingproperty/DeclaredBase.js";
import client_1 from "./connect-client.default.js";
async function send_1(init?: EndpointRequestInit_1): Promise<DeclaredBase_1 | undefined> { return client_1.call("ExistingPropertyEndpoint", "send", {}, init); }
export { send_1 as send };
