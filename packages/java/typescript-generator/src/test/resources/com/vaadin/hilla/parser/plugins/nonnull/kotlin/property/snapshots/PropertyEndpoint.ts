import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import type Person_1 from "./com/vaadin/hilla/parser/plugins/nonnull/kotlin/property/Person.js";
import client_1 from "./connect-client.default.js";
async function get_1(id: number, init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PropertyEndpoint", "get", { id }, init); }
export { get_1 as get };
