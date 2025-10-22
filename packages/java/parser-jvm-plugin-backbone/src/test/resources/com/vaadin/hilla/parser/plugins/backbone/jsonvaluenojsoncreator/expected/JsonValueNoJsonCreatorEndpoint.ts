import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import client_1 from "./connect-client.default.js";
async function getEmail_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("JsonValueNoJsonCreatorEndpoint", "getEmail", {}, init); }
async function setEmail_1(email: string | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonValueNoJsonCreatorEndpoint", "setEmail", { email }, init); }
export { getEmail_1 as getEmail, setEmail_1 as setEmail };

