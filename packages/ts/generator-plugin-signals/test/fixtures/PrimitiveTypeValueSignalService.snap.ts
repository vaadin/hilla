import { ArrayModel as ArrayModel_1, BooleanModel as BooleanModel_1, NumberModel as NumberModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import { ValueSignal as ValueSignal_1 } from "@vaadin/hilla-react-signals";
import client_1 from "./connect-client.default.js";
function anotherStringValueSignal_1(options?: {
    defaultValue: string;
}): ValueSignal_1<string> {
    return new ValueSignal_1(options?.defaultValue ?? StringModel_1.createEmptyValue(), { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "anotherStringValueSignal" });
}
function booleanValueSignal_1(options?: {
    defaultValue: boolean;
}): ValueSignal_1<boolean> {
    return new ValueSignal_1(options?.defaultValue ?? BooleanModel_1.createEmptyValue(), { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "booleanValueSignal" });
}
function booleanValueSignalNullable_1(options?: {
    defaultValue: boolean | undefined;
}): ValueSignal_1<boolean | undefined> {
    return new ValueSignal_1(options?.defaultValue ?? undefined, { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "booleanValueSignalNullable" });
}
function doubleValueSignal_1(options?: {
    defaultValue: number;
}): ValueSignal_1<number> {
    return new ValueSignal_1(options?.defaultValue ?? NumberModel_1.createEmptyValue(), { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "doubleValueSignal" });
}
function doubleValueSignalNullable_1(options?: {
    defaultValue: number | undefined;
}): ValueSignal_1<number | undefined> {
    return new ValueSignal_1(options?.defaultValue ?? undefined, { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "doubleValueSignalNullable" });
}
function stringArrayValueSignal_1(options?: {
    defaultValue: Array<string>;
}): ValueSignal_1<Array<string>> {
    return new ValueSignal_1(options?.defaultValue ?? ArrayModel_1.createEmptyValue(), { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "stringArrayValueSignal" });
}
function stringArrayValueSignalNullable_1(options?: {
    defaultValue: Array<string | undefined>;
}): ValueSignal_1<Array<string | undefined>> {
    return new ValueSignal_1(options?.defaultValue ?? ArrayModel_1.createEmptyValue(), { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "stringArrayValueSignalNullable" });
}
function stringValueSignal_1(options?: {
    defaultValue: string;
}): ValueSignal_1<string> {
    return new ValueSignal_1(options?.defaultValue ?? StringModel_1.createEmptyValue(), { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "stringValueSignal" });
}
function stringValueSignalNullable_1(options?: {
    defaultValue: string | undefined;
}): ValueSignal_1<string | undefined> {
    return new ValueSignal_1(options?.defaultValue ?? undefined, { client: client_1, endpoint: "PrimitiveTypeValueSignalService", method: "stringValueSignalNullable" });
}
export { anotherStringValueSignal_1 as anotherStringValueSignal, booleanValueSignal_1 as booleanValueSignal, booleanValueSignalNullable_1 as booleanValueSignalNullable, doubleValueSignal_1 as doubleValueSignal, doubleValueSignalNullable_1 as doubleValueSignalNullable, stringArrayValueSignal_1 as stringArrayValueSignal, stringArrayValueSignalNullable_1 as stringArrayValueSignalNullable, stringValueSignal_1 as stringValueSignal, stringValueSignalNullable_1 as stringValueSignalNullable };
