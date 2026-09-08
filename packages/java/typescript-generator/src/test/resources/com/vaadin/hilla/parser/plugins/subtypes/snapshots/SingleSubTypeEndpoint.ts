import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type SoleBase_1 from "./com/vaadin/hilla/parser/plugins/subtypes/singlesubtype/SoleBase.js";
import client_1 from "./connect-client.default.js";
async function send_1(init?: EndpointRequestInit_1): Promise<SoleBase_1 | undefined> { return client_1.call("SingleSubTypeEndpoint", "send", {}, init); }
export { send_1 as send };
