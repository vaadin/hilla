import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import type Person_1 from "./com/github/taefi/data/Person.js";
import client_1 from "./connect-client.default.js";
async function getPerson_1(init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PersonService", "getPerson", {}, init); }
function personSignal_1() {
    return new ValueSignal_1(undefined, { client: client_1, endpoint: "PersonService", method: "personSignal" });
}
export { getPerson_1 as getPerson, personSignal_1 as personSignal };
