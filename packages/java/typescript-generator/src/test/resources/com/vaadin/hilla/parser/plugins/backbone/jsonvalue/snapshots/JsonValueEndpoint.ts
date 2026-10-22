import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Person from "./com/vaadin/hilla/parser/plugins/backbone/jsonvalue/JsonValueEndpoint/Person.js";
import client from "./connect-client.default.js";
async function getEmail(init?: EndpointRequestInit): Promise<string | undefined> { return client.call("JsonValueEndpoint", "getEmail", {}, init); }
async function getPerson(init?: EndpointRequestInit): Promise<Person | undefined> { return client.call("JsonValueEndpoint", "getPerson", {}, init); }
async function setEmail(email: string | undefined, init?: EndpointRequestInit): Promise<void> { return client.call("JsonValueEndpoint", "setEmail", { email }, init); }
async function setPerson(person: Person | undefined, init?: EndpointRequestInit): Promise<void> { return client.call("JsonValueEndpoint", "setPerson", { person }, init); }
export { getEmail, getPerson, setEmail, setPerson };
