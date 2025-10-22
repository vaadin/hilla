import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type TransientModel_1 from "./com/vaadin/hilla/parser/plugins/backbone/transients/TransientEndpoint/TransientModel.js";
import client_1 from "./connect-client.default.js";
async function getTransientModel_1(init?: EndpointRequestInit_1): Promise<TransientModel_1 | undefined> { return client_1.call("TransientEndpoint", "getTransientModel", {}, init); }
export { getTransientModel_1 as getTransientModel };
