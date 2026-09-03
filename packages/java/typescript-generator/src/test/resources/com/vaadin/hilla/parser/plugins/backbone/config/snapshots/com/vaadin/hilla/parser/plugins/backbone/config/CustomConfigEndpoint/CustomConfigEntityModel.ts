import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type CustomConfigEntity_1 from "./CustomConfigEntity.js";
class CustomConfigEntityModel<T extends CustomConfigEntity_1 = CustomConfigEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CustomConfigEntityModel);
    get bar(): NumberModel_1 {
        return this[_getPropertyModel_1]("bar", (parent, key) => new NumberModel_1(parent, key, false, { meta: { javaType: "int" } }));
    }
    get foo(): StringModel_1 {
        return this[_getPropertyModel_1]("foo", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CustomConfigEntityModel;
