import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Nested from "./Nested.js";
import NestedModel_1 from "./NestedModel.js";
class NestedModel<T extends Nested = Nested> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NestedModel);
    get numberMatrix(): ArrayModel<ArrayModel<NumberModel>> {
        return this[_getPropertyModel]("numberMatrix", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Integer" } }), { meta: { javaType: "java.util.List" } }), { meta: { javaType: "java.util.List" } }));
    }
    get entityMatrix(): ArrayModel<ArrayModel<NestedModel_1>> {
        return this[_getPropertyModel]("entityMatrix", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new NestedModel_1(parent, key, true), { meta: { javaType: "java.util.List" } }), { meta: { javaType: "java.util.List" } }));
    }
    get stringMatrix(): ArrayModel<ArrayModel<StringModel>> {
        return this[_getPropertyModel]("stringMatrix", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.lang.String[]" } }), { meta: { javaType: "java.lang.String[][]" } }));
    }
    get objectMap(): ObjectModel<Record<string, unknown>> {
        return this[_getPropertyModel]("objectMap", (parent, key) => new ObjectModel(parent, key, true, { meta: { javaType: "java.util.Map" } }));
    }
}
export default NestedModel;
