import { _getPropertyModel, ArrayModel, makeObjectEmptyValueCreator, StringModel } from "@vaadin/hilla-lit-form";
import type ExtendedNonNullableModel from "./ExtendedNonNullableModel.js";
import NonNullableModelModel from "./NonNullableModelModel.js";
class ExtendedNonNullableModelModel<T extends ExtendedNonNullableModel = ExtendedNonNullableModel> extends NonNullableModelModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ExtendedNonNullableModelModel);
    get mixedAnnotations(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("mixedAnnotations", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get nonTypeAnnotation(): StringModel {
        return this[_getPropertyModel]("nonTypeAnnotation", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ExtendedNonNullableModelModel;
