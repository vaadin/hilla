import { ArrayModel, BooleanModel, NumberModel, StringModel } from "@vaadin/hilla-lit-form";
import { SignalMethodOptions, ValueSignal } from "@vaadin/hilla-react-signals";
import client from "./connect-client.default.js";
function anotherStringValueSignal(options?: SignalMethodOptions<string>): ValueSignal<string> { return new ValueSignal(options?.defaultValue ?? StringModel.createEmptyValue(), {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "anotherStringValueSignal"
}); }
function booleanValueSignal(options?: SignalMethodOptions<boolean>): ValueSignal<boolean> { return new ValueSignal(options?.defaultValue ?? BooleanModel.createEmptyValue(), {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "booleanValueSignal"
}); }
function booleanValueSignalNullable(options?: SignalMethodOptions<boolean | undefined>): ValueSignal<boolean | undefined> { return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "booleanValueSignalNullable"
}); }
function doubleValueSignal(options?: SignalMethodOptions<number>): ValueSignal<number> { return new ValueSignal(options?.defaultValue ?? NumberModel.createEmptyValue(), {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "doubleValueSignal"
}); }
function doubleValueSignalNullable(options?: SignalMethodOptions<number | undefined>): ValueSignal<number | undefined> { return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "doubleValueSignalNullable"
}); }
function stringArrayValueSignal(options?: SignalMethodOptions<Array<string>>): ValueSignal<Array<string>> { return new ValueSignal(options?.defaultValue ?? ArrayModel.createEmptyValue(), {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "stringArrayValueSignal"
}); }
function stringArrayValueSignalNullable(options?: SignalMethodOptions<Array<string | undefined>>): ValueSignal<Array<string | undefined>> { return new ValueSignal(options?.defaultValue ?? ArrayModel.createEmptyValue(), {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "stringArrayValueSignalNullable"
}); }
function stringValueSignal(options?: SignalMethodOptions<string>): ValueSignal<string> { return new ValueSignal(options?.defaultValue ?? StringModel.createEmptyValue(), {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "stringValueSignal"
}); }
function stringValueSignalNullable(options?: SignalMethodOptions<string | undefined>): ValueSignal<string | undefined> { return new ValueSignal(options?.defaultValue, {
    client: client,
    endpoint: "PrimitiveTypeValueSignalService",
    method: "stringValueSignalNullable"
}); }
export { anotherStringValueSignal, booleanValueSignal, booleanValueSignalNullable, doubleValueSignal, doubleValueSignalNullable, stringArrayValueSignal, stringArrayValueSignalNullable, stringValueSignal, stringValueSignalNullable };
