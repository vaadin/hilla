import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Person_1 from "./com/vaadin/hilla/parser/plugins/backbone/jsonvalue/JsonValueEndpoint/Person.js";
import client_1 from "./connect-client.default.js";
async function getEmail_1(init?: EndpointRequestInit_1): Promise<string | undefined> { return client_1.call("JsonValueEndpoint", "getEmail", {}, init); }
async function getPerson_1(init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("JsonValueEndpoint", "getPerson", {}, init); }
async function setEmail_1(email: string | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonValueEndpoint", "setEmail", { email }, init); }
async function setPerson_1(person: Person_1 | undefined, init?: EndpointRequestInit_1): Promise<void> { return client_1.call("JsonValueEndpoint", "setPerson", { person }, init); }
export { getEmail_1 as getEmail, getPerson_1 as getPerson, setEmail_1 as setEmail, setPerson_1 as setPerson };
