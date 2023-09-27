import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, StringModel as StringModel_1 } from "@hilla/form";
import type FormEntityMetadata_1 from "./FormEntityMetadata.js";
class FormEntityMetadataModel<T extends FormEntityMetadata_1 = FormEntityMetadata_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormEntityMetadataModel);
    get withoutMetadata(): StringModel_1 {
        return this[_getPropertyModel_1]("withoutMetadata", (parent, key) => new StringModel_1(parent, key, false));
    }
    get withJavaType(): StringModel_1 {
        return this[_getPropertyModel_1]("withJavaType", (parent, key) => new StringModel_1(parent, key, false, { meta: { javaType: "java.time.LocalDateTime" } }));
    }
    get listWithJavaType(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("listWithJavaType", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.time.LocalDateTime" } }), { meta: { javaType: "java.util.List" } }));
    }
    get withAnnotations(): NumberModel_1 {
        return this[_getPropertyModel_1]("withAnnotations", (parent, key) => new NumberModel_1(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.Id" }, { name: "jakarta.persistence.Version" }] } }));
    }
    get listWithAnnotations(): ArrayModel_1<NumberModel_1> {
        return this[_getPropertyModel_1]("listWithAnnotations", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new NumberModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }, { name: "jakarta.persistence.Version" }] } }), { meta: { javaType: "java.util.List" } }));
    }
    get withAll(): NumberModel_1 {
        return this[_getPropertyModel_1]("withAll", (parent, key) => new NumberModel_1(parent, key, false, { meta: { annotations: [{ name: "jakarta.persistence.Id" }, { name: "jakarta.persistence.Version" }], javaType: "java.lang.Long" } }));
    }
}
export default FormEntityMetadataModel;
