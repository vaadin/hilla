import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type CustomConfigEntity_1 from "./com/vaadin/hilla/parser/plugins/backbone/config/CustomConfigEndpoint/CustomConfigEntity.js";
import client_1 from "./connect-client.default.js";
async function get_1(init?: EndpointRequestInit_1): Promise<CustomConfigEntity_1 | undefined> { return client_1.call("CustomConfigEndpoint", "get", {}, init); }
export { get_1 as get };
