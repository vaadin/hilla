import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type CustomEntity_1 from "./CustomEntity.js";
class CustomEntityModel<T extends CustomEntity_1 = CustomEntity_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(CustomEntityModel);
    get value(): StringModel_1 {
        return this[_getPropertyModel_1]("value", (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default CustomEntityModel;
