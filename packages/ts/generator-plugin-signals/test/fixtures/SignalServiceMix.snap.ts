import { EndpointRequestInit as EndpointRequestInit_1 } from "@vaadin/hilla-frontend";
import { ArrayModel as ArrayModel_1 } from "@vaadin/hilla-lit-form";
import { ListSignal as ListSignal_1, ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import type Person_1 from "./com/github/taefi/data/Person.js";
import PersonModel_1 from "./com/github/taefi/data/PersonModel.js";
import client_1 from "./connect-client.default.js";
async function getPerson_1(init?: EndpointRequestInit_1): Promise<Person_1 | undefined> { return client_1.call("PersonService", "getPerson", {}, init); }
function personArraySignal_1(options?: {
    defaultValue: Array<Person_1>;
}): ValueSignal_1<Array<Person_1>> {
    return new ValueSignal_1(options?.defaultValue ?? ArrayModel_1.createEmptyValue(), { client: client_1, endpoint: "PersonService", method: "personArraySignal" });
}
function personListSignal_1(): ListSignal_1<Person_1> {
    return new ListSignal_1({ client: client_1, endpoint: "PersonService", method: "personListSignal" });
}
function personSignalNotNull_1(options?: {
    defaultValue: Person_1;
}): ValueSignal_1<Person_1> {
    return new ValueSignal_1(options?.defaultValue ?? PersonModel_1.createEmptyValue(), { client: client_1, endpoint: "PersonService", method: "personSignalNotNull" });
}
function personSignalNullable_1(isAdult: boolean, options?: {
    defaultValue: Person_1 | undefined;
}): ValueSignal_1<Person_1 | undefined> {
    return new ValueSignal_1(options?.defaultValue ?? undefined, { client: client_1, endpoint: "PersonService", method: "personSignalNullable", params: { isAdult } });
}
function personSignalWithParams_1(dummyBoolean: boolean, dummyString: string | undefined, options?: {
    defaultValue: Person_1 | undefined;
}): ValueSignal_1<Person_1 | undefined> {
    return new ValueSignal_1(options?.defaultValue ?? undefined, { client: client_1, endpoint: "PersonService", method: "personSignalWithParams", params: { dummyBoolean, dummyString } });
}
function personSignalNonNullWithParams_1(dummyBoolean: boolean, dummyString: string | undefined, options?: {
    defaultValue: Person_1;
}): ValueSignal_1<Person_1> {
    return new ValueSignal_1(options?.defaultValue ?? PersonModel_1.createEmptyValue(), { client: client_1, endpoint: "PersonService", method: "personSignalNonNullWithParams", params: { dummyBoolean, dummyString } });
}
export { getPerson_1 as getPerson, personArraySignal_1 as personArraySignal, personListSignal_1 as personListSignal, personSignalNonNullWithParams_1 as personSignalNonNullWithParams, personSignalNotNull_1 as personSignalNotNull, personSignalNullable_1 as personSignalNullable, personSignalWithParams_1 as personSignalWithParams };
