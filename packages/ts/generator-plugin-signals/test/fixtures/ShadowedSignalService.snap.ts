import { SignalMethodOptions, ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import type Person_1 from "./com/github/taefi/data/other/Person.js";
import PersonModel_1 from "./com/github/taefi/data/other/PersonModel.js";
import type Person from "./com/github/taefi/data/Person.js";
import PersonModel from "./com/github/taefi/data/PersonModel.js";
import client from "./connect-client.default.js";
function personSignal(options?: SignalMethodOptions<Person>): ValueSignal_1<Person> { return new ValueSignal_1(options?.defaultValue ?? PersonModel.createEmptyValue(), {
    client: client,
    endpoint: "ShadowedSignalService",
    method: "personSignal"
}); }
function otherPersonSignal(ValueSignal: string, options?: SignalMethodOptions<Person_1>): ValueSignal_1<Person_1> { return new ValueSignal_1(options?.defaultValue ?? PersonModel_1.createEmptyValue(), {
    client: client,
    endpoint: "ShadowedSignalService",
    method: "otherPersonSignal",
    params: { ValueSignal }
}); }
export { otherPersonSignal, personSignal };
