import { EndpointRequestInit } from "@vaadin/hilla-frontend";
import { $defaultValue, ArrayModel } from "@vaadin/hilla-models";
import { ListSignal, SignalMethodOptions, ValueSignal } from "@vaadin/hilla-react-signals";
import type Person from "./com/github/taefi/data/Person.js";
import PersonModel from "./com/github/taefi/data/PersonModel.js";
import client from "./connect-client.default.js";
async function getPerson(init?: EndpointRequestInit): Promise<Person | undefined> { return client.call("PersonService", "getPerson", {}, init); }
function personArraySignal(options?: SignalMethodOptions<Array<Person>>): ValueSignal<Array<Person>> { return new ValueSignal(options?.defaultValue ?? ArrayModel[$defaultValue], {
    client: client,
    endpoint: "PersonService",
    method: "personArraySignal"
}); }
function personListSignal(): ListSignal<Person> { return new ListSignal({
    client: client,
    endpoint: "PersonService",
    method: "personListSignal"
}); }
function personSignalNotNull(options?: SignalMethodOptions<Person>): ValueSignal<Person> { return new ValueSignal(options?.defaultValue ?? PersonModel[$defaultValue], {
    client: client,
    endpoint: "PersonService",
    method: "personSignalNotNull"
}); }
function personSignalNullable(isAdult: boolean, options?: SignalMethodOptions<Person | undefined>): ValueSignal<Person | undefined> { return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: "PersonService",
    method: "personSignalNullable",
    params: { isAdult }
}); }
function personSignalWithParams(dummyBoolean: boolean, dummyString: string | undefined, options?: SignalMethodOptions<Person | undefined>): ValueSignal<Person | undefined> { return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: "PersonService",
    method: "personSignalWithParams",
    params: { dummyBoolean, dummyString }
}); }
function personSignalNonNullWithParams(dummyBoolean: boolean, dummyString: string | undefined, options?: SignalMethodOptions<Person>): ValueSignal<Person> { return new ValueSignal(options?.defaultValue ?? PersonModel[$defaultValue], {
    client: client,
    endpoint: "PersonService",
    method: "personSignalNonNullWithParams",
    params: { dummyBoolean, dummyString }
}); }
export { getPerson, personArraySignal, personListSignal, personSignalNonNullWithParams, personSignalNotNull, personSignalNullable, personSignalWithParams };
