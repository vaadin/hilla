import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import type Person from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/property/Person.js";
import client from "./connect-client.default.js";
async function get(id: number, init?: EndpointRequestInit): Promise<Person | undefined> { return client.call("PropertyEndpoint", "get", { id }, init); }
export { get };
