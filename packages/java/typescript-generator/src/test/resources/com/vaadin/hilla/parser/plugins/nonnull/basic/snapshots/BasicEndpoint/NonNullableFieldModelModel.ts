import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type NonNullableFieldModel from "./NonNullableFieldModel.js";
class NonNullableFieldModelModel<T extends NonNullableFieldModel = NonNullableFieldModel> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(NonNullableFieldModelModel);
    get stringList(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("stringList", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
}
export default NonNullableFieldModelModel;
