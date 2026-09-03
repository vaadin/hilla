import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type ExtendedNonNullableModel_1 from "./ExtendedNonNullableModel.js";
import NonNullableModelModel_1 from "./NonNullableModelModel.js";
class ExtendedNonNullableModelModel<T extends ExtendedNonNullableModel_1 = ExtendedNonNullableModel_1> extends NonNullableModelModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ExtendedNonNullableModelModel);
    get mixedAnnotations(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("mixedAnnotations", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.util.List" } }));
    }
    get nonTypeAnnotation(): StringModel_1 {
        return this[_getPropertyModel_1]("nonTypeAnnotation", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
}
export default ExtendedNonNullableModelModel;
