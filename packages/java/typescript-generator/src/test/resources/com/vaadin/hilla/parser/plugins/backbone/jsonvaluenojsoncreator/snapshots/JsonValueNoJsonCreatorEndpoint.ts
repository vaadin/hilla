import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import client from "./connect-client.default.js";
async function getEmail(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("JsonValueNoJsonCreatorEndpoint", "getEmail", {}, init); }
async function setEmail(email: string | undefined, init?: EndpointRequestInit): Promise<void> { return client.call("JsonValueNoJsonCreatorEndpoint", "setEmail", { email }, init); }
export { getEmail, setEmail };
