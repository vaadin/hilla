import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type NonNullableFieldModel_1 from "./NonNullableFieldModel.js";
class NonNullableFieldModelModel<T extends NonNullableFieldModel_1 = NonNullableFieldModel_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(NonNullableFieldModelModel);
    get stringList(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("stringList", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
}
export default NonNullableFieldModelModel;
