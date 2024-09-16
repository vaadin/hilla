import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import type Person_1 from "./com/github/taefi/data/Person.js";
import client_1 from "./connect-client.default.js";
async function getPerson_1(init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PersonService", "getPerson", {}, init); }
function personSignal_1(): ValueSignal_1<Person_1 | undefined> {
    return new ValueSignal_1(undefined, { client: client_1, endpoint: "PersonService", method: "personSignal" });
}
function personSignalNonNull_1({ defaultValue: defaultValue_1 }: {
    defaultValue: Person_1;
}): ValueSignal_1<Person_1> {
    return new ValueSignal_1(defaultValue_1, { client: client_1, endpoint: "PersonService", method: "personSignalNonNull" });
}
export { getPerson_1 as getPerson, personSignal_1 as personSignal, personSignalNonNull_1 as personSignalNonNull };
