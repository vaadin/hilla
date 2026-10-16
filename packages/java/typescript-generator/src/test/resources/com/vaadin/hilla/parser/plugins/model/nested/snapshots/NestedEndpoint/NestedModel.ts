import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Nested_1 from "./Nested.js";
import NestedModel_1 from "./NestedModel.js";
class NestedModel<T extends Nested_1 = Nested_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NestedModel);
    get numberMatrix(): ArrayModel_1<ArrayModel_1<NumberModel_1>> {
        return this[_getPropertyModel_1]("numberMatrix", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NumberModel_1(parent, key, true, { meta: { javaType: "java.lang.Integer" } }), { meta: { javaType: "java.util.List" } }), { meta: { javaType: "java.util.List" } }));
    }
    get entityMatrix(): ArrayModel_1<ArrayModel_1<NestedModel_1>> {
        return this[_getPropertyModel_1]("entityMatrix", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new NestedModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }), { meta: { javaType: "java.util.List" } }));
    }
    get stringMatrix(): ArrayModel_1<ArrayModel_1<StringModel_1>> {
        return this[_getPropertyModel_1]("stringMatrix", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.lang.String[]" } }), { meta: { javaType: "java.lang.String[][]" } }));
    }
    get objectMap(): ObjectModel_1<Record<string, unknown>> {
        return this[_getPropertyModel_1]("objectMap", (parent, key) => new ObjectModel_1(parent, key, true, { meta: { javaType: "java.util.Map" } }));
    }
}
export default NestedModel;
